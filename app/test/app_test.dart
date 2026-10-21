import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gamemaps/app.dart';

void main() {
  testWidgets('shows an honest navigation setup screen', (tester) async {
    await tester.pumpWidget(const GameMapsApp());

    expect(find.text('GameMaps'), findsOneWidget);
    expect(find.text('Navigation setup pending'), findsOneWidget);
    expect(find.text('Start navigation'), findsNothing);
    expect(find.byType(MaterialApp), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('offers a map preview without starting guidance', (tester) async {
    await tester.pumpWidget(const GameMapsApp());

    final previewButton = find.widgetWithText(FilledButton, 'Open map');
    expect(previewButton, findsOneWidget);
    expect(tester.widget<FilledButton>(previewButton).onPressed, isNotNull);
    expect(find.text('GameMaps'), findsOneWidget);
    expect(find.text('Navigation setup pending'), findsOneWidget);
    expect(find.text('Start navigation'), findsNothing);
    expect(tester.takeException(), isNull);
  });
}
