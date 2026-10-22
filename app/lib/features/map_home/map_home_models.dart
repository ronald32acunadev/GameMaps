import 'package:flutter/widgets.dart';

@immutable
class GeoPoint {
  GeoPoint(this.latitude, this.longitude) {
    if (!latitude.isFinite ||
        !longitude.isFinite ||
        latitude.abs() > 90 ||
        longitude.abs() > 180) {
      throw RangeError('Invalid map coordinates');
    }
  }
  final double latitude;
  final double longitude;
  @override
  bool operator ==(Object other) =>
      other is GeoPoint &&
      latitude == other.latitude &&
      longitude == other.longitude;
  @override
  int get hashCode => Object.hash(latitude, longitude);
}

@immutable
class Destination {
  const Destination({
    required this.placeId,
    required this.name,
    required this.address,
    required this.position,
  });
  final String placeId;
  final String name;
  final String? address;
  final GeoPoint position;
}

enum LocationStatus { available, denied, disabled, unavailable }

@immutable
class LocationResult {
  const LocationResult.available(GeoPoint point, {this.approximate = false})
    : status = LocationStatus.available,
      position = point;
  const LocationResult.denied()
    : status = LocationStatus.denied,
      position = null,
      approximate = false;
  const LocationResult.disabled()
    : status = LocationStatus.disabled,
      position = null,
      approximate = false;
  const LocationResult.unavailable()
    : status = LocationStatus.unavailable,
      position = null,
      approximate = false;
  final LocationStatus status;
  final GeoPoint? position;
  final bool approximate;
}

enum SearchStatus { selected, cancelled, failed }

@immutable
class SearchResult {
  const SearchResult.selected(Destination value)
    : status = SearchStatus.selected,
      destination = value,
      category = null;
  const SearchResult.cancelled()
    : status = SearchStatus.cancelled,
      destination = null,
      category = null;
  const SearchResult.failed(this.category)
    : status = SearchStatus.failed,
      destination = null;
  final SearchStatus status;
  final Destination? destination;
  final String? category;
}

@immutable
class MapHomeMapState {
  const MapHomeMapState({
    required this.currentPosition,
    required this.locationGranted,
    required this.destination,
    required this.cameraTarget,
    required this.cameraRevision,
  });
  final GeoPoint? currentPosition;
  final bool locationGranted;
  final Destination? destination;
  final GeoPoint cameraTarget;
  final int cameraRevision;
}

typedef MapSurfaceBuilder = Widget Function(BuildContext, MapHomeMapState);
