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
}
