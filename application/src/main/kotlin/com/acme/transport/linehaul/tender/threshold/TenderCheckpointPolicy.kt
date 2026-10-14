package com.acme.transport.linehaul.tender.threshold

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Counts how many amounts fit in a budget of 24119.0, in order. */
fun tenderCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24119.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tenderCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun tenderCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun tenderCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
