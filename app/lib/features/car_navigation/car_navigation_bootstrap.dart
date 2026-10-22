import 'dart:async';

import 'package:flutter/services.dart';

class CarHostState {
  const CarHostState(this.revision, this.phoneAttached, this.carAttached);

  final int revision;
  final bool phoneAttached;
  final bool carAttached;

  static CarHostState? decode(Object? value) {
    if (value is! Map) return null;
    final revision = value['revision'];
    final phone = value['phoneAttached'];
    final car = value['carAttached'];
    if (revision is! int || revision < 0 || phone is! bool || car is! bool) {
      return null;
    }
    return CarHostState(revision, phone, car);
  }
}

/// Owns engine startup, not navigation or a phone widget's lifecycle.
class CarNavigationBootstrap {
  CarNavigationBootstrap({
    this.channel = const MethodChannel('gamemaps/car_navigation'),
    required this.onPhoneAttached,
  });

  final MethodChannel channel;
  final void Function() onPhoneAttached;
  Future<void>? _startFuture;
  bool _phoneMounted = false;
  bool _disposed = false;
  CarHostState? snapshot;
  String? failure;

  Future<void> start() => _startFuture ??= _start();

  Future<void> _start() async {
    if (_disposed) return;
    channel.setMethodCallHandler((call) async {
      if (call.method != 'hostChanged') throw MissingPluginException();
      await _accept(call.arguments);
    });
    try {
      final initial = await _invoke('getHostState');
      if (_disposed) return;
      await _accept(initial);
      if (!_disposed && snapshot != null && failure == null) {
        await _invoke('bootstrapReady');
      }
    } catch (_) {
      if (!_disposed) failure = 'unavailable';
    }
  }

  Future<void> _accept(Object? value) async {
    if (_disposed) return;
    final next = CarHostState.decode(value);
    if (next == null) {
      failure = 'protocol';
      return;
    }
    if (next.revision <= (snapshot?.revision ?? -1)) return;
    snapshot = next;
    failure = null;
    if (next.phoneAttached && !_phoneMounted) {
      _phoneMounted = true;
      onPhoneAttached();
    }
    try {
      await _invoke('ackHostState', {'revision': next.revision});
    } catch (_) {
      if (!_disposed) failure = 'unavailable';
    }
  }

  Future<Object?> _invoke(String method, [Object? arguments]) => channel
      .invokeMethod<Object?>(method, arguments)
      .timeout(const Duration(seconds: 5));

  Future<void> dispose() async {
    _disposed = true;
    channel.setMethodCallHandler(null);
  }
}
