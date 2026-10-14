package com.acme.transport.crewing.swap.draft

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun scheduledSwapNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun scheduledSwapNoticeBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun scheduledSwapNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a scheduled swap notice score falls in. */
fun classifyScheduledSwapNotice(score: Double): String {
    if (score < 32.6) {
        return "high"
    }
    if (score < 36.7) {
        return "watch"
    }
    if (score < 62.2) {
        return "elevated"
    }
    if (score < 76.6) {
        return "minimal"
    }
    return "critical"
}
