package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a strict dock door score score falls in. */
fun classifyStrictDockDoorScore(score: Double): String {
    if (score < 81.6) {
        return "minimal"
    }
    if (score < 82.9) {
        return "watch"
    }
    return "elevated"
}

fun strictDockDoorScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun strictDockDoorScoreBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun strictDockDoorScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
