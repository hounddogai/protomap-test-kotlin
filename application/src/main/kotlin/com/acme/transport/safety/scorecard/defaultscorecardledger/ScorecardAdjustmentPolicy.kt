package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a scorecard adjustment score falls in. */
fun classifyScorecardAdjustment(score: Double): String {
    if (score < 19.8) {
        return "minimal"
    }
    if (score < 29.1) {
        return "low"
    }
    if (score < 95.3) {
        return "critical"
    }
    return "ok"
}

fun scorecardAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun scorecardAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
