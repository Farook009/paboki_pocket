import 'package:flutter_test/flutter_test.dart';
import 'package:paboki_pocket/models/signal_result.dart';

void main() {
  test('confidence is clamped to 55-75', () {
    final high = SignalResult.fromMap({'direction': 'UP', 'confidence': 99});
    final low = SignalResult.fromMap({'direction': 'DOWN', 'confidence': 10});
    expect(high.confidence, 75);
    expect(low.confidence, 55);
    expect(high.isUp, isTrue);
  });
}
