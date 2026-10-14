package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.numbers.roundTo

fun cameraEventVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun cameraEventVarianceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 31136.0, in order. */
fun cameraEventVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 31136.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
