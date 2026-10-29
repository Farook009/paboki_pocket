import 'package:flutter/material.dart';

class OverlayApp extends StatelessWidget {
  const OverlayApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        backgroundColor: Colors.transparent,
        body: Center(
          child: Text(
            "Paboki Overlay Running",
            style: TextStyle(color: Colors.white),
          ),
        ),
      ),
    );
  }
}