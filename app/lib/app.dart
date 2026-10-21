import 'package:flutter/material.dart';

import 'features/map_preview/map_preview_page.dart';

class GameMapsApp extends StatelessWidget {
  const GameMapsApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'GameMaps',
      home: Scaffold(
        appBar: AppBar(title: const Text('GameMaps')),
        body: SafeArea(
          child: Center(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Text('Navigation setup pending'),
                const SizedBox(height: 16),
                Builder(
                  builder: (context) => FilledButton(
                    onPressed: () => Navigator.of(context).push<void>(
                      MaterialPageRoute<void>(
                        builder: (_) => const MapPreviewPage(),
                      ),
                    ),
                    child: const Text('Open map'),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
