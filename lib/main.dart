import 'package:flutter/material.dart';
import 'home_screen.dart';
import 'overlay/overlay_app.dart';

@pragma('vm:entry-point')
void overlayMain() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MaterialApp(
    debugShowCheckedModeBanner: false,
    home: OverlayApp(),
  ));
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const PabokiApp());
}

class PabokiApp extends StatelessWidget {
  const PabokiApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Paboki Pocket',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const HomeScreen(),
    );
  }
}