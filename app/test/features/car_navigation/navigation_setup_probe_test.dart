import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gamemaps/features/car_navigation/navigation_setup_probe.dart';

class FakeSetupGateway implements NavigationSetupGateway {
  bool accepted = true;
  bool acceptsDialog = true;
  Object? initializationError;
  bool cleanupFails = false;
  final calls = <String>[];
  Completer<void>? pending;
  @override
  Future<bool> termsAccepted() async {
    calls.add('terms');
    return accepted;
  }

  @override
  Future<bool> showTerms() async {
    calls.add('dialog');
    return acceptsDialog;
  }

  @override
  Future<void> initialize() async {
    calls.add('initialize');
    if (initializationError case final error?) throw error;
    await pending?.future;
  }

  @override
  Future<void> cleanup() async {
    calls.add('cleanup');
    if (cleanupFails) throw StateError('synthetic');
  }
}

void main() {
  test('headless setup cannot display terms or initialize', () async {
    final gateway = FakeSetupGateway();
    final result = await NavigationSetupProbe(gateway)
        .run(phoneAttached: false);
    expect(result['status'], 'needs_phone');
    expect(result['destinationRequests'], 0);
    expect(gateway.calls, isEmpty);
  });
  test('human declining terms makes no session request', () async {
    final gateway = FakeSetupGateway()
      ..accepted = false
      ..acceptsDialog = false;
    final result = await NavigationSetupProbe(gateway).run(phoneAttached: true);
    expect(result['status'], 'terms_declined');
    expect(gateway.calls, ['terms', 'dialog']);
  });
  test('successful setup is one-shot and always cleans up', () async {
    final gateway = FakeSetupGateway()..pending = Completer<void>();
    final probe = NavigationSetupProbe(gateway);
    final first = probe.run(phoneAttached: true);
    final duplicate = probe.run(phoneAttached: true);
    gateway.pending!.complete();
    expect((await first)['status'], 'ready');
    expect(await duplicate, await first);
    expect(gateway.calls, ['terms', 'initialize', 'cleanup']);
    expect((await first)['destinationRequests'], 0);
  });
  test(
    'authorization failure is sanitized and cleaned up without route retries',
    () async {
      final gateway = FakeSetupGateway()
        ..initializationError = PlatformException(
          code: 'notAuthorized',
          message: 'sensitive detail',
        );
      final result = await NavigationSetupProbe(gateway)
          .run(phoneAttached: true);
      expect(result['status'], 'not_authorized');
      expect(result.toString(), isNot(contains('sensitive detail')));
      expect(result['destinationRequests'], 0);
      expect(gateway.calls, ['terms', 'initialize', 'cleanup']);
    },
  );
  test('cleanup failure is not reported as successful setup', () async {
    final gateway = FakeSetupGateway()..cleanupFails = true;
    final result = await NavigationSetupProbe(gateway).run(phoneAttached: true);
    expect(result['status'], 'cleanup_failed');
    expect(result['cleanup'], false);
  });
}
