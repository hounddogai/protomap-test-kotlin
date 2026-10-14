package com.acme.transport.kit.numbers

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

// Arithmetic helpers: clamping, rounding, and simple statistics.

fun clamp(value: Int, low: Int, high: Int): Int {
    if (value < low) {
        return low
    }
    if (value > high) {
        return high
    }
    return value
}

fun clampFloat(value: Double, low: Double, high: Double): Double = minOf(maxOf(value, low), high)

/** Returns part as a percentage of whole, rounded to two places, and 0 when whole is 0. */
fun percent(part: Double, whole: Double): Double {
    if (whole == 0.0) {
        return 0.0
    }
    return round(part / whole * 100.0 * 100) / 100
}

fun roundTo(value: Double, step: Double): Double {
    if (step <= 0.0) {
        return value
    }
    return floor(value / step + 0.5) * step
}

fun mean(values: MutableList<Double>): Double {
    if (values.size == 0) {
        return 0.0
    }
    var total = 0.0
    for (value in values) {
        total += value
    }
    return total / values.size.toDouble()
}

/** Returns the values in ascending order, leaving the input as it was. */
fun sortedFloats(values: MutableList<Double>): MutableList<Double> {
    val sorted = values.toMutableList()
    for (i in 0 until sorted.size) {
        for (k in 0 until sorted.size - i - 1) {
            if (sorted[k] > sorted[k + 1]) {
                val swap: Double = sorted[k]
                sorted[k] = sorted[k + 1]
                sorted[k + 1] = swap
            }
        }
    }
    return sorted
}

/** Returns the p-th percentile by the nearest-rank method. */
fun percentile(values: MutableList<Double>, p: Double): Double {
    if (values.size == 0) {
        return 0.0
    }
    val sorted = sortedFloats(values)
    val rank = (p / 100.0 * (sorted.size - 1).toDouble()).toInt()
    return sorted[clamp(rank, 0, sorted.size - 1)]
}

fun weightedMean(values: MutableList<Double>, weights: MutableList<Double>): Double {
    if (values.size != weights.size) {
        error("got ${values.size} values but ${weights.size} weights")
    }
    var sum = 0.0
    var weight = 0.0
    for (i in 0 until values.size) {
        sum += values[i] * weights[i]
        weight += weights[i]
    }
    if (weight == 0.0) {
        return 0.0
    }
    return sum / weight
}

fun growthRate(previous: Double, current: Double): Double {
    if (previous == 0.0) {
        return 0.0
    }
    return round((current - previous) / abs(previous) * 100) / 100
}
