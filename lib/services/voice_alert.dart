import 'package:flutter_tts/flutter_tts.dart';
import '../models/signal_result.dart';

class VoiceAlertService {
  static final instance = VoiceAlertService._internal();
  factory VoiceAlertService() => instance;
  VoiceAlertService._internal() {
    _init();
  }

  final FlutterTts _tts = FlutterTts();

  Future<void> _init() async {
    await _tts.setLanguage("en-US");
    await _tts.setSpeechRate(0.5);
  }

  Future<void> speak(String text) async {
    await _tts.speak(text);
  }

  Future<void> stop() async {
    await _tts.stop();
  }

  Future<void> announce(SignalResult result) async {
    await speak("${result.direction} signal");
  }

  Future<void> startAssistant() async {
    await speak("Voice Assistant Started");
  }

  Future<void> startAssistantService() async {
    await speak("Service Started");
  }
}