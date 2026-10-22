import 'package:flutter/foundation.dart';

import 'map_home_gateway.dart';
import 'map_home_models.dart';

class MapHomeController extends ChangeNotifier {
  MapHomeController(this._gateway);
  final MapHomeGateway _gateway;
  LocationResult _location = const LocationResult.unavailable();
  Destination? _destination;
  GeoPoint _target = GeoPoint(9.9281, -84.0907);
  int _revision = 0;
  bool _disposed = false;
  bool _initialized = false;
  bool locating = false;
  bool searching = false;
  String? searchMessage;

  MapHomeMapState get mapState => MapHomeMapState(
    currentPosition: _location.position,
    locationGranted: _location.status == LocationStatus.available,
    destination: _destination,
    cameraTarget: _target,
    cameraRevision: _revision,
  );

  String? get locationMessage {
    if (locating) return 'Finding your location…';
    return switch (_location.status) {
      LocationStatus.available =>
        _location.approximate ? 'Approximate location' : null,
      LocationStatus.denied =>
        'Location permission is off. Search is still available.',
      LocationStatus.disabled =>
        'Location services are off. Search is still available.',
      LocationStatus.unavailable =>
        'Location is unavailable. Search is still available.',
    };
  }

  Future<void> initialize() async {
    if (_initialized || _disposed) return;
    _initialized = true;
    await _locate(initial: true);
  }

  Future<void> recenter() => _locate(initial: false);

  Future<void> refreshLocation() =>
      _locate(initial: true, requestPermission: false);

  Future<void> _locate({
    required bool initial,
    bool requestPermission = true,
  }) async {
    if (locating || _disposed) return;
    final revision = _revision;
    locating = true;
    notifyListeners();
    LocationResult value;
    try {
      value = await _gateway.locate(requestPermission: requestPermission);
    } catch (_) {
      value = const LocationResult.unavailable();
    }
    if (_disposed) return;
    _location = value;
    locating = false;
    if (value.position != null &&
        (!initial || (_revision == revision && _destination == null))) {
      _target = value.position!;
      _revision++;
    }
    notifyListeners();
  }

  Future<void> search() async {
    if (searching || _disposed) return;
    searching = true;
    searchMessage = null;
    notifyListeners();
    SearchResult value;
    try {
      value = await _gateway.search(bias: _location.position);
    } catch (_) {
      value = const SearchResult.failed('unavailable');
    }
    if (_disposed) return;
    searching = false;
    if (value.status == SearchStatus.selected) {
      _destination = value.destination!;
      _target = _destination!.position;
      _revision++;
    } else if (value.status == SearchStatus.failed) {
      searchMessage = switch (value.category) {
        'network' =>
          'Search needs a network connection. Try again when connected.',
        'credentials' => 'Place search configuration needs attention.',
        'quota' => 'Place search usage limit reached. Try again later.',
        'resolution' =>
          'Could not load this destination. Choose another place.',
        _ => 'Place search is unavailable.',
      };
    }
    notifyListeners();
  }

  void clearDestination() {
    if (_disposed) return;
    _destination = null;
    searchMessage = null;
    notifyListeners();
  }

  @override
  void dispose() {
    _disposed = true;
    super.dispose();
  }
}
