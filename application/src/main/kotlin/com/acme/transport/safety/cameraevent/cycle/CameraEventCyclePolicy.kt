package com.acme.transport.safety.cameraevent.cycle

/** Names the band a camera event cycle score falls in. */
fun classifyCameraEventCycle(score: Double): String {
    if (score < 11.8) {
        return "elevated"
    }
    if (score < 15.0) {
        return "critical"
    }
    if (score < 30.7) {
        return "medium"
    }
    return "ok"
}

fun cameraEventCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Counts how many amounts fit in a budget of 11810.0, in order. */
fun cameraEventCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11810.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
