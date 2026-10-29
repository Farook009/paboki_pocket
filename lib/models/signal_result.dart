class SignalResult {
  const SignalResult({
    required this.direction,
    required this.confidence,
    this.detail = '',
  });

  final String direction; // UP or DOWN
  final int confidence; // 55-75
  final String detail;

  bool get isUp => direction == 'UP';

  static const expiryLabel = '1 Minute';
  static const expiryClock = '00:01:00';

  factory SignalResult.fromMap(Map<dynamic, dynamic> map) {
    final raw = (map['confidence'] as num?)?.toInt() ?? 60;
    final confidence = raw.clamp(55, 75);
    final direction = (map['direction'] as String?) == 'UP' ? 'UP' : 'DOWN';
    return SignalResult(
      direction: direction,
      confidence: confidence,
      detail: (map['detail'] as String?) ?? '',
    );
  }

  Map<String, dynamic> toMap() => {
        'direction': direction,
        'confidence': confidence,
        'detail': detail,
        'expiry': expiryClock,
      };
}
