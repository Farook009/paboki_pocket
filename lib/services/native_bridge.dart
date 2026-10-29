import 'package:flutter/services.dart';
import 'package:paboki_pocket/models/signal_result.dart';

class NativeBridge {
  NativeBridge._();

  static const MethodChannel _channel = MethodChannel('com.paboki.pocket/capture');

  static Future<bool> prepareCapture() async {
    final ok = await _channel.invokeMethod<bool>('prepareCapture');
    return ok ?? false;
  }

  static Future<bool> hasCaptureSession() async {
    final ok = await _channel.invokeMethod<bool>('hasCaptureSession');
    return ok ?? false;
  }

  static Future<SignalResult> analyzeChart() async {
    final raw = await _channel.invokeMethod<Map<dynamic, dynamic>>('analyzeChart');
    if (raw == null) {
      return const SignalResult(
        direction: 'DOWN',
        confidence: 55,
        detail: 'No analysis data',
      );
    }
    return SignalResult.fromMap(raw);
  }
}
