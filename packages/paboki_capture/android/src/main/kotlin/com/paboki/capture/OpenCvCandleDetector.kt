package com.paboki.capture

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * OpenCV-style candle reader:
 * RGB -> HSV, inRange for green/red bodies, morphology-style cleanup,
 * then rightmost blobs as the newest candles.
 *
 * Pocket Option expiry for this assistant is always 1 minute.
 */
object OpenCvCandleDetector {

    data class Result(
        val direction: String,
        val confidence: Int,
        val detail: String,
        val expiry: String = "00:01:00",
    )

    fun analyze(fullScreen: Bitmap): Result {
        val chart = cropCenter(fullScreen, 0.70f)
        val candles = detectCandles(chart)
        chart.recycle()
        return decide(candles)
    }

    private fun cropCenter(source: Bitmap, fraction: Float): Bitmap {
        val w = source.width
        val h = source.height
        val cropW = max(8, (w * fraction).roundToInt())
        val cropH = max(8, (h * fraction).roundToInt())
        val x = (w - cropW) / 2
        val y = (h - cropH) / 2
        return Bitmap.createBitmap(source, x, y, cropW, cropH)
    }

    private data class Candle(val x: Int, val isGreen: Boolean, val body: Int)

    private fun detectCandles(chart: Bitmap): List<Candle> {
        val width = chart.width
        val height = chart.height
        val columns = 24
        val colW = max(2, width / columns)
        val startX = (width * 0.45f).toInt()
        val y0 = (height * 0.12f).toInt()
        val y1 = (height * 0.88f).toInt()
        val found = mutableListOf<Candle>()

        var x = startX
        while (x + colW < width) {
            var green = 0
            var red = 0
            var cy = y0
            while (cy < y1) {
                var cx = x
                val xEnd = x + colW
                while (cx < xEnd) {
                    val pixel = chart.getPixel(cx, cy)
                    if (Color.alpha(pixel) > 20) {
                        when (classify(pixel)) {
                            1 -> green++
                            -1 -> red++
                        }
                    }
                    cx += 2
                }
                cy += 2
            }
            val total = green + red
            if (total > 18) {
                found.add(Candle(x + colW / 2, green >= red, max(green, red)))
            }
            x += colW
        }
        return found.takeLast(8)
    }

    /** HSV inRange equivalent of OpenCV green/red candle masks. */
    private fun classify(pixel: Int): Int {
        val r = Color.red(pixel) / 255f
        val g = Color.green(pixel) / 255f
        val b = Color.blue(pixel) / 255f
        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        val v = maxC
        val s = if (maxC == 0f) 0f else (maxC - minC) / maxC
        if (v < 0.18f || s < 0.28f) return 0
        val d = maxC - minC
        var h = 0f
        if (d > 0f) {
            h = when (maxC) {
                r -> 60f * (((g - b) / d) % 6f)
                g -> 60f * (((b - r) / d) + 2f)
                else -> 60f * (((r - g) / d) + 4f)
            }
            if (h < 0) h += 360f
        }
        // Green / teal up-candles (Pocket Option style).
        if (h in 70f..175f && g > r * 1.05f) return 1
        // Red down-candles.
        if ((h <= 18f || h >= 345f) && r > g * 1.08f) return -1
        return 0
    }

    private fun decide(candles: List<Candle>): Result {
        val lastThree = candles.takeLast(3)
        if (lastThree.isEmpty()) {
            return Result("DOWN", 55, "No candles detected in chart crop")
        }

        val greenCount = lastThree.count { it.isGreen }
        val greenBody = lastThree.filter { it.isGreen }.sumOf { it.body }.toFloat()
        val redBody = lastThree.filter { !it.isGreen }.sumOf { it.body }.toFloat()
        val denom = greenBody + redBody
        val rsi = if (denom <= 0f) 50f else 100f * greenBody / denom

        val up = greenCount >= 2 && rsi >= 48f
        val direction = if (up) "UP" else "DOWN"

        var confidence = 55
        confidence += min(10, abs(greenCount * 2 - 3) * 4)
        confidence += min(8, (abs(rsi - 50f) / 50f * 8f).roundToInt())
        val last = lastThree.last()
        if ((up && last.isGreen) || (!up && !last.isGreen)) confidence += 4
        if (lastThree.size == 3 && last.body >= lastThree[1].body) confidence += 3
        confidence = min(75, max(55, confidence))

        val detail =
            "candles=${lastThree.size} green=$greenCount rsi=${rsi.roundToInt()} expiry=00:01:00"
        return Result(direction, confidence, detail)
    }
}
