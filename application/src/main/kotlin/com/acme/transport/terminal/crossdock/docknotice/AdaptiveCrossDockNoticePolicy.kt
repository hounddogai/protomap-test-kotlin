package com.acme.transport.terminal.crossdock.docknotice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a adaptive cross dock notice score falls in. */
fun classifyAdaptiveCrossDockNotice(score: Double): String {
    if (score < 56.9) {
        return "ok"
    }
    if (score < 91.3) {
        return "low"
    }
    return "minimal"
}

fun mergeAdaptiveCrossDockNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 72071.0, in order. */
fun adaptiveCrossDockNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 72071.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveCrossDockNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.47
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
