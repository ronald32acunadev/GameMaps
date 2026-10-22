import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gamemaps/features/car_navigation/car_navigation_bootstrap.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  const channel = MethodChannel('gamemaps/car_navigation');
  final messenger =
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger;
  const codec = StandardMethodCodec();

  Future<void> emit(Map<String, Object> state) async {
    final completed = Completer<void>();
    await messenger.handlePlatformMessage(
      channel.name,
      codec.encodeMethodCall(MethodCall('hostChanged', state)),
      (_) => completed.complete(),
    );
    await completed.future;
  }

  tearDown(() => messenger.setMockMethodCallHandler(channel, null));

  test(
    'car-first startup never mounts the phone or invokes navigation',
    () async {
      final calls = <String>[];
      messenger.setMockMethodCallHandler(channel, (call) async {
        calls.add(call.method);
        if (call.method == 'getHostState') {
          return {'revision': 1, 'phoneAttached': false, 'carAttached': true};
        }
        return null;
      });
      var mounted = 0;
      final bootstrap = CarNavigationBootstrap(
        channel: channel,
        onPhoneAttached: () => mounted++,
      );
      await bootstrap.start();
      await bootstrap.start();
      expect(mounted, 0);
      expect(calls, ['getHostState', 'ackHostState', 'bootstrapReady']);
      expect(bootstrap.snapshot?.carAttached, isTrue);
      expect(bootstrap.snapshot?.phoneAttached, isFalse);
      await bootstrap.dispose();
    },
  );

  test(
    'phone attachment mounts once and ignores stale initial snapshot',
    () async {
      final initial = Completer<Map<String, Object>>();
      var reads = 0;
      messenger.setMockMethodCallHandler(channel, (call) async {
        if (call.method == 'getHostState') {
          reads++;
          return initial.future;
        }
        return null;
      });
      var mounted = 0;
      final bootstrap = CarNavigationBootstrap(
        channel: channel,
        onPhoneAttached: () => mounted++,
      );
      final pending = bootstrap.start();
      await emit({'revision': 2, 'phoneAttached': true, 'carAttached': true});
      initial.complete({
        'revision': 0,
        'phoneAttached': false,
        'carAttached': false,
      });
      await pending;
      await emit({'revision': 3, 'phoneAttached': false, 'carAttached': true});
      await emit({'revision': 4, 'phoneAttached': true, 'carAttached': true});
      expect(reads, 1);
      expect(mounted, 1);
      expect(bootstrap.snapshot?.revision, 4);
      await bootstrap.dispose();
    },
  );

  test('disposed bootstrap ignores a pending host response', () async {
    final initial = Completer<Map<String, Object>>();
    messenger.setMockMethodCallHandler(channel, (call) async => initial.future);
    var mounted = 0;
    final bootstrap = CarNavigationBootstrap(
      channel: channel,
      onPhoneAttached: () => mounted++,
    );
    final pending = bootstrap.start();
    await bootstrap.dispose();
    initial.complete({
      'revision': 1,
      'phoneAttached': true,
      'carAttached': false,
    });
    await pending;
    expect(mounted, 0);
    expect(bootstrap.snapshot, isNull);
  });

  test('invalid host data is not acknowledged as ready', () async {
    final calls = <String>[];
    messenger.setMockMethodCallHandler(channel, (call) async {
      calls.add(call.method);
      return {'revision': -1, 'phoneAttached': 'true', 'carAttached': true};
    });
    var mounted = 0;
    final bootstrap = CarNavigationBootstrap(
      channel: channel,
      onPhoneAttached: () => mounted++,
    );
    await bootstrap.start();
    expect(calls, ['getHostState']);
    expect(mounted, 0);
    expect(bootstrap.snapshot, isNull);
    expect(bootstrap.failure, 'protocol');
    await bootstrap.dispose();
  });
}
