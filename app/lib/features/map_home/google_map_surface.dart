import 'dart:async';

import 'package:flutter/material.dart';
import 'package:google_navigation_flutter/google_navigation_flutter.dart';

import 'map_home_models.dart';

class GoogleMapSurface extends StatefulWidget {
  const GoogleMapSurface({super.key, required this.state});
  final MapHomeMapState state;
  @override
  State<GoogleMapSurface> createState() => _GoogleMapSurfaceState();
}

class _GoogleMapSurfaceState extends State<GoogleMapSurface> {
  GoogleMapViewController? _controller;
  List<Marker> _markers = [];
  Destination? _destination;
  bool? _locationEnabled;
  bool _applying = false;
  int _requested = 0;
  int _applied = -1;
  int _cameraRevision = -1;
  LatLng _latLng(GeoPoint p) =>
      LatLng(latitude: p.latitude, longitude: p.longitude);

  @override
  void didUpdateWidget(covariant GoogleMapSurface oldWidget) {
    super.didUpdateWidget(oldWidget);
    _requested++;
    unawaited(_apply());
  }

  Future<void> _apply() async {
    final controller = _controller;
    if (controller == null || _applying || !mounted) return;
    _applying = true;
    try {
      while (mounted && _applied != _requested) {
        final requested = _requested;
        final state = widget.state;
        if (_locationEnabled != state.locationGranted) {
          await controller.setMyLocationEnabled(state.locationGranted);
          if (!mounted) return;
          _locationEnabled = state.locationGranted;
        }
        if (_destination != state.destination) {
          if (_markers.isNotEmpty) await controller.removeMarkers(_markers);
          if (!mounted) return;
          _markers = [];
          if (state.destination != null) {
            final added = await controller.addMarkers([
              MarkerOptions(position: _latLng(state.destination!.position)),
            ]);
            if (!mounted) return;
            _markers = added.whereType<Marker>().toList();
          }
          _destination = state.destination;
        }
        if (_cameraRevision != state.cameraRevision) {
          await controller.moveCamera(
            CameraUpdate.newLatLngZoom(
              _latLng(state.cameraTarget),
              state.cameraRevision == 0 ? 12 : 15,
            ),
          );
          if (!mounted) return;
          _cameraRevision = state.cameraRevision;
        }
        _applied = requested;
      }
    } catch (_) {
      _applied = _requested;
      if (mounted) {
        ScaffoldMessenger.maybeOf(context)?.showSnackBar(
          const SnackBar(
            content: Text(
              'Map controls are unavailable. Reopen the app to retry.',
            ),
          ),
        );
      }
    } finally {
      _applying = false;
    }
  }

  @override
  Widget build(BuildContext context) => GoogleMapsMapView(
    initialCameraPosition: CameraPosition(
      target: _latLng(widget.state.cameraTarget),
      zoom: 12,
    ),
    initialMapToolbarEnabled: false,
    onViewCreated: (controller) {
      if (!mounted) return;
      _controller = controller;
      unawaited(_apply());
    },
  );
}
