import 'dart:async';

import 'package:flutter/widgets.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'app.dart';
import 'features/car_navigation/car_navigation_bootstrap.dart';
import 'features/car_navigation/navigation_setup_probe.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  final bootstrap = CarNavigationBootstrap(
    onPhoneAttached: () => runApp(const GameMapsApp()),
  );
  if (kDebugMode) {
    final setup = NavigationSetupProbe(GoogleNavigationSetupGateway());
    const MethodChannel('gamemaps/navigation_setup_probe')
        .setMethodCallHandler((call) async {
          if (call.method != 'runSetup') throw MissingPluginException();
          return setup.run(
            phoneAttached: bootstrap.snapshot?.phoneAttached == true,
          );
        });
  }
  unawaited(bootstrap.start());
}
