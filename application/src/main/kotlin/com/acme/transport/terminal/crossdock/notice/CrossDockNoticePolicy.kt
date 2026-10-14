package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 10273.0, in order. */
fun crossDockNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10273.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a cross dock notice score falls in. */
fun classifyCrossDockNotice(score: Double): String {
    if (score < 72.6) {
        return "elevated"
    }
    if (score < 97.6) {
        return "minimal"
    }
    return "high"
}

fun crossDockNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.62
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun crossDockNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
