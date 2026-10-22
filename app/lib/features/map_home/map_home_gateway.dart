import 'map_home_models.dart';

abstract interface class MapHomeGateway {
  Future<LocationResult> locate({required bool requestPermission});
  Future<SearchResult> search({GeoPoint? bias});
}
