import 'package:flutter/material.dart';

import 'map_home_controller.dart';
import 'map_home_gateway.dart';
import 'map_home_models.dart';

class MapHomePage extends StatefulWidget {
  const MapHomePage({
    super.key,
    required this.gateway,
    required this.mapBuilder,
  });
  final MapHomeGateway gateway;
  final MapSurfaceBuilder mapBuilder;
  @override
  State<MapHomePage> createState() => _MapHomePageState();
}

class _MapHomePageState extends State<MapHomePage> with WidgetsBindingObserver {
  late final MapHomeController _controller;
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _controller = MapHomeController(widget.gateway);
    _controller.initialize();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _controller.dispose();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _controller.refreshLocation();
    }
  }

  @override
  Widget build(BuildContext context) => ListenableBuilder(
    listenable: _controller,
    builder: (context, _) {
      final state = _controller.mapState;
      final destination = state.destination;
      return Scaffold(
        body: SafeArea(
          child: Column(
            children: [
              Padding(
                padding: const EdgeInsets.all(12),
                child: Row(
                  children: [
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: _controller.searching
                            ? null
                            : _controller.search,
                        icon: const Icon(Icons.search),
                        label: Text(
                          _controller.searching
                              ? 'Searching…'
                              : 'Search places',
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    IconButton.filledTonal(
                      tooltip: 'Recenter',
                      onPressed: _controller.locating
                          ? null
                          : _controller.recenter,
                      icon: const Icon(Icons.my_location),
                    ),
                  ],
                ),
              ),
              if (_controller.locationMessage case final String message)
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 0, 16, 8),
                  child: Text(message),
                ),
              Expanded(child: widget.mapBuilder(context, state)),
              if (_controller.searchMessage case final String message)
                Padding(
                  padding: const EdgeInsets.all(12),
                  child: Text(message, semanticsLabel: message),
                ),
              if (destination != null)
                ListTile(
                  leading: const Icon(Icons.place),
                  title: Text(destination.name),
                  subtitle: destination.address == null
                      ? null
                      : Text(destination.address!),
                  trailing: IconButton(
                    tooltip: 'Clear destination',
                    onPressed: _controller.clearDestination,
                    icon: const Icon(Icons.close),
                  ),
                ),
            ],
          ),
        ),
      );
    },
  );
}
