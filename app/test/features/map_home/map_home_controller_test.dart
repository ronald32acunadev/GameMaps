import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:gamemaps/features/map_home/map_home_controller.dart';
import 'package:gamemaps/features/map_home/map_home_gateway.dart';
import 'package:gamemaps/features/map_home/map_home_models.dart';

class Gateway implements MapHomeGateway {
  Future<LocationResult> location = Future.value(const LocationResult.denied());
  Future<SearchResult> selection = Future.value(const SearchResult.cancelled());
  int searches = 0;
  int locations = 0;
  @override
  Future<LocationResult> locate({required bool requestPermission}) {
    expect(requestPermission, isTrue);
    locations++;
    return location;
  }

  @override
  Future<SearchResult> search({GeoPoint? bias}) {
    searches++;
    return selection;
  }
}

Destination place(String id) => Destination(
  placeId: id,
  name: 'Public destination',
  address: null,
  position: GeoPoint(9.9, -84.1),
);

void main() {
  test('rejects non-finite and out-of-range coordinates', () {
    for (final pair in [
      [double.nan, 0.0],
      [0.0, double.infinity],
      [91.0, 0.0],
      [0.0, -181.0],
    ]) {
      expect(() => GeoPoint(pair[0], pair[1]), throwsRangeError);
    }
  });
  test(
    'approximate grant is available and recenter uses foreground position',
    () async {
      final g = Gateway()
        ..location = Future.value(
          LocationResult.available(GeoPoint(9.8, -84.0), approximate: true),
        );
      final c = MapHomeController(g);
      await c.initialize();
      expect(c.mapState.locationGranted, isTrue);
      expect(c.mapState.cameraTarget, GeoPoint(9.8, -84.0));
      expect(c.locationMessage, contains('Approximate'));
      await c.recenter();
      expect(g.locations, 2);
      expect(c.mapState.cameraRevision, 2);
      c.dispose();
    },
  );
  test(
    'denied disabled and unavailable location preserve map and search',
    () async {
      for (final r in [
        const LocationResult.denied(),
        const LocationResult.disabled(),
        const LocationResult.unavailable(),
      ]) {
        final g = Gateway()..location = Future.value(r);
        final c = MapHomeController(g);
        await c.initialize();
        expect(c.mapState.currentPosition, isNull);
        expect(c.mapState.locationGranted, isFalse);
        expect(c.locationMessage, isNotNull);
        await c.search();
        expect(g.searches, 1);
        expect(c.searching, isFalse);
        c.dispose();
      }
    },
  );
  test(
    'select clear cancellation and failed replacement preserve correct state',
    () async {
      final g = Gateway()
        ..selection = Future.value(SearchResult.selected(place('one')));
      final c = MapHomeController(g);
      await c.search();
      final selected = c.mapState.destination;
      expect(selected?.placeId, 'one');
      expect(c.mapState.cameraTarget, selected?.position);
      g.selection = Future.value(const SearchResult.cancelled());
      await c.search();
      expect(c.mapState.destination, same(selected));
      g.selection = Future.value(const SearchResult.failed('credentials'));
      await c.search();
      expect(c.mapState.destination, same(selected));
      expect(c.searchMessage, contains('configuration'));
      c.clearDestination();
      expect(c.mapState.destination, isNull);
      expect(c.mapState.cameraTarget, selected?.position);
      c.dispose();
    },
  );
  test(
    'initial location never overrides a destination chosen while waiting',
    () async {
      final fix = Completer<LocationResult>();
      final g = Gateway()
        ..location = fix.future
        ..selection = Future.value(SearchResult.selected(place('one')));
      final c = MapHomeController(g);
      final initial = c.initialize();
      await c.search();
      final target = c.mapState.cameraTarget;
      fix.complete(LocationResult.available(GeoPoint(10, -85)));
      await initial;
      expect(c.mapState.cameraTarget, target);
      expect(c.mapState.currentPosition, GeoPoint(10, -85));
      c.dispose();
    },
  );
  test(
    'duplicate taps launch one search and completion after dispose is ignored',
    () async {
      final pending = Completer<SearchResult>();
      final g = Gateway()..selection = pending.future;
      final c = MapHomeController(g);
      var notifications = 0;
      c.addListener(() => notifications++);
      final first = c.search();
      await c.search();
      expect(g.searches, 1);
      c.dispose();
      final before = notifications;
      pending.complete(SearchResult.selected(place('late')));
      await first;
      expect(notifications, before);
      expect(c.mapState.destination, isNull);
    },
  );
  test('resume refresh rechecks location without prompting or stealing destination camera', () async {
    final calls = <bool>[];
    final g = ResumeGateway(calls);
    final c = MapHomeController(g);
    await c.initialize();
    await c.search();
    final target = c.mapState.cameraTarget;
    await c.refreshLocation();
    expect(calls, [true, false]);
    expect(c.mapState.cameraTarget, target);
    expect(c.mapState.currentPosition, GeoPoint(10, -85));
    c.dispose();
  });
  test(
    'location exception clears pending without fabricating a position',
    () async {
      final g = Gateway()..location = Future.error(StateError('native failed'));
      final c = MapHomeController(g);
      await c.initialize();
      expect(c.locating, isFalse);
      expect(c.mapState.currentPosition, isNull);
      expect(c.locationMessage, contains('unavailable'));
      c.dispose();
    },
  );
}

class ResumeGateway implements MapHomeGateway {
  ResumeGateway(this.calls);
  final List<bool> calls;
  @override
  Future<LocationResult> locate({required bool requestPermission}) async {
    calls.add(requestPermission);
    return LocationResult.available(GeoPoint(10, -85));
  }

  @override
  Future<SearchResult> search({GeoPoint? bias}) async =>
      SearchResult.selected(place('chosen'));
}
