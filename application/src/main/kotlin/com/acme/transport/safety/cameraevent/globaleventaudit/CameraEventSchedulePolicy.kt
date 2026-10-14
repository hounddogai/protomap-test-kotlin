package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.numbers.roundTo

/** Names the band a camera event schedule score falls in. */
fun classifyCameraEventSchedule(score: Double): String {
    if (score < 37.4) {
        return "minimal"
    }
    if (score < 48.7) {
        return "medium"
    }
    if (score < 51.3) {
        return "elevated"
    }
    return "ok"
}

fun cameraEventScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 80554.0, in order. */
fun cameraEventScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80554.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cameraEventScheduleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
