import 'package:flutter/services.dart';
import 'package:google_navigation_flutter/google_navigation_flutter.dart';

abstract interface class NavigationSetupGateway {
  Future<bool> termsAccepted();
  Future<bool> showTerms();
  Future<void> initialize();
  Future<void> cleanup();
}

/// Zero-destination preflight: this interface deliberately cannot calculate routes.
class NavigationSetupProbe {
  NavigationSetupProbe(this.gateway);
  final NavigationSetupGateway gateway;
  Future<Map<String, Object>>? _once;

  Future<Map<String, Object>> run({required bool phoneAttached}) {
    if (!phoneAttached) {
      return Future.value({
        'status': 'needs_phone',
        'destinationRequests': 0,
        'cleanup': true,
      });
    }
    return _once ??= _check();
  }

  Future<Map<String, Object>> _check() async {
    var status = 'unavailable';
    var attempted = false;
    var cleaned = true;
    try {
      final accepted =
          await gateway.termsAccepted() || await gateway.showTerms();
      if (!accepted) {
        status = 'terms_declined';
      } else {
        attempted = true;
        await gateway.initialize();
        status = 'ready';
      }
    } on PlatformException catch (error) {
      status = switch (error.code) {
        'notAuthorized' => 'not_authorized',
        'locationPermissionMissing' => 'permission_missing',
        'termsNotAccepted' => 'terms_required',
        _ => 'unavailable',
      };
    } catch (_) {
      status = 'unavailable';
    } finally {
      if (attempted) {
        try {
          await gateway.cleanup();
        } catch (_) {
          cleaned = false;
          status = 'cleanup_failed';
        }
      }
    }
    return {'status': status, 'destinationRequests': 0, 'cleanup': cleaned};
  }
}

class GoogleNavigationSetupGateway implements NavigationSetupGateway {
  @override
  Future<bool> termsAccepted() => GoogleMapsNavigator.areTermsAccepted();
  @override
  Future<bool> showTerms() => GoogleMapsNavigator.showTermsAndConditionsDialog(
    'GameMaps navigation setup',
    'GameMaps',
  );
  @override
  Future<void> initialize() => GoogleMapsNavigator.initializeNavigationSession(
    taskRemovedBehavior: TaskRemovedBehavior.quitService,
  );
  @override
  Future<void> cleanup() async {
    if (await GoogleMapsNavigator.isInitialized()) {
      await GoogleMapsNavigator.cleanup();
    }
  }
}
