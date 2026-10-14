package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun rosterScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun rosterScoreBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a roster score score falls in. */
fun classifyRosterScore(score: Double): String {
    if (score < 58.0) {
        return "medium"
    }
    if (score < 58.3) {
        return "critical"
    }
    if (score < 62.6) {
        return "minimal"
    }
    return "watch"
}

fun rosterScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
