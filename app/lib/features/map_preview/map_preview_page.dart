import 'package:flutter/material.dart';
import 'package:google_navigation_flutter/google_navigation_flutter.dart';

class MapPreviewPage extends StatelessWidget {
  const MapPreviewPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Map preview')),
      body: SafeArea(
        child: GoogleMapsMapView(
          initialCameraPosition: const CameraPosition(
            target: LatLng(latitude: 9.9281, longitude: -84.0907),
            zoom: 12,
          ),
          initialMapToolbarEnabled: false,
          onViewCreated: (_) {},
        ),
      ),
    );
  }
}
