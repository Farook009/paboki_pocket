import 'package:flutter/material.dart';
import 'services/voice_alert.dart';


class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final voice = VoiceAlertService.instance;

  @override
  void initState() {
    super.initState();
    voice.startAssistant();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Paboki Pocket")),
      body: Center(
        child: ElevatedButton(
          onPressed: () {
            voice.speak("Signal Checking");
          },
          child: const Text("Test Voice"),
        ),
      ),
    );
  }
}