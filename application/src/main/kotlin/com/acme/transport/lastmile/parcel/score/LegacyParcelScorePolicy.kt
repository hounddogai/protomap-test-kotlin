package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a legacy parcel score score falls in. */
fun classifyLegacyParcelScore(score: Double): String {
    if (score < 20.9) {
        return "ok"
    }
    if (score < 39.9) {
        return "high"
    }
    if (score < 46.9) {
        return "critical"
    }
    return "watch"
}

fun legacyParcelScoreBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun legacyParcelScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.66
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 44851.0, in order. */
fun legacyParcelScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44851.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
