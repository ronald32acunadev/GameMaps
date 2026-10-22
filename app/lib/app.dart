import 'package:flutter/material.dart';

import 'features/map_home/google_map_surface.dart';
import 'features/map_home/map_home_gateway.dart';
import 'features/map_home/map_home_models.dart';
import 'features/map_home/map_home_page.dart';
import 'features/map_home/method_channel_map_home_gateway.dart';

class GameMapsApp extends StatelessWidget {
  const GameMapsApp({super.key, this.gateway, this.mapBuilder});
  final MapHomeGateway? gateway;
  final MapSurfaceBuilder? mapBuilder;
  @override
  Widget build(BuildContext context) => MaterialApp(
    title: 'GameMaps',
    home: MapHomePage(
      gateway: gateway ?? const MethodChannelMapHomeGateway(),
      mapBuilder: mapBuilder ?? (_, state) => GoogleMapSurface(state: state),
    ),
  );
}
