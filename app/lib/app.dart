import 'package:flutter/material.dart';

class GameMapsApp extends StatelessWidget {
  const GameMapsApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'GameMaps',
      home: Scaffold(
        appBar: AppBar(title: const Text('GameMaps')),
        body: const SafeArea(
          child: Center(child: Text('Navigation setup pending')),
        ),
      ),
    );
  }
}
