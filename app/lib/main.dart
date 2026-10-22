import 'dart:async';

import 'package:flutter/widgets.dart';

import 'app.dart';
import 'features/car_navigation/car_navigation_bootstrap.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  final bootstrap = CarNavigationBootstrap(
    onPhoneAttached: () => runApp(const GameMapsApp()),
  );
  unawaited(bootstrap.start());
}
