import 'package:flutter/services.dart';

import 'map_home_gateway.dart';
import 'map_home_models.dart';

class MethodChannelMapHomeGateway implements MapHomeGateway {
  const MethodChannelMapHomeGateway();
  static const _channel = MethodChannel('gamemaps/map_home');

  GeoPoint _point(Map<Object?, Object?> value) => GeoPoint(
    (value['latitude'] as num).toDouble(),
    (value['longitude'] as num).toDouble(),
  );

  @override
  Future<LocationResult> locate({required bool requestPermission}) async {
    try {
      final value = await _channel.invokeMapMethod<Object?, Object?>('locate', {
        'requestPermission': requestPermission,
      });
      if (value == null) return const LocationResult.unavailable();
      return switch (value['status']) {
        'available' => LocationResult.available(
          _point(value),
          approximate: value['approximate'] == true,
        ),
        'denied' => const LocationResult.denied(),
        'disabled' => const LocationResult.disabled(),
        _ => const LocationResult.unavailable(),
      };
    } catch (_) {
      return const LocationResult.unavailable();
    }
  }

  @override
  Future<SearchResult> search({GeoPoint? bias}) async {
    try {
      final value = await _channel.invokeMapMethod<Object?, Object?>('search', {
        if (bias != null) 'latitude': bias.latitude,
        if (bias != null) 'longitude': bias.longitude,
      });
      if (value == null) return const SearchResult.failed('unavailable');
      if (value['status'] == 'cancelled') return const SearchResult.cancelled();
      if (value['status'] == 'failed') {
        final category = value['category'];
        return SearchResult.failed(
          const {
                'network',
                'credentials',
                'quota',
                'resolution',
              }.contains(category)
              ? category as String
              : 'unavailable',
        );
      }
      if (value['status'] != 'selected') {
        return const SearchResult.failed('unavailable');
      }
      final id = value['placeId'] as String;
      final name = value['name'] as String;
      if (id.trim().isEmpty || name.trim().isEmpty) {
        return const SearchResult.failed('resolution');
      }
      return SearchResult.selected(
        Destination(
          placeId: id,
          name: name,
          address: value['address'] as String?,
          position: _point(value),
        ),
      );
    } catch (_) {
      return const SearchResult.failed('unavailable');
    }
  }
}
