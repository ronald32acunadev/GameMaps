import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gamemaps/app.dart';
import 'package:gamemaps/features/map_home/map_home_gateway.dart';
import 'package:gamemaps/features/map_home/map_home_models.dart';

class UnavailableGateway implements MapHomeGateway {
  @override
  Future<LocationResult> locate({required bool requestPermission}) async =>
      const LocationResult.unavailable();
  @override
  Future<SearchResult> search({GeoPoint? bias}) async =>
      const SearchResult.failed('unavailable');
}

void main() {
  testWidgets(
    'opens map home without launcher and keeps unavailable map usable',
    (tester) async {
      await tester.pumpWidget(
        GameMapsApp(
          gateway: UnavailableGateway(),
          mapBuilder: (_, state) =>
              const ColoredBox(key: Key('map-surface'), color: Colors.green),
        ),
      );
      await tester.pumpAndSettle();
      expect(find.byKey(const Key('map-surface')), findsOneWidget);
      expect(find.byType(MaterialApp), findsOneWidget);
      expect(find.text('Open map'), findsNothing);
      expect(find.text('Navigation setup pending'), findsNothing);
      expect(find.text('Map preview'), findsNothing);
      expect(find.text('Start navigation'), findsNothing);
      expect(find.text('Search places'), findsOneWidget);
      expect(find.byTooltip('Recenter'), findsOneWidget);
      expect(find.textContaining('Location is unavailable'), findsOneWidget);
      await tester.tap(find.text('Search places'));
      await tester.pumpAndSettle();
      expect(find.text('Place search is unavailable.'), findsOneWidget);
      expect(find.byKey(const Key('map-surface')), findsOneWidget);
      expect(tester.takeException(), isNull);
    },
  );
}
