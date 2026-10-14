package com.acme.transport.safety.cameraevent.eventadjustment

/** Names the band a strict camera event cycle score falls in. */
fun classifyStrictCameraEventCycle(score: Double): String {
    if (score < 58.0) {
        return "low"
    }
    if (score < 92.3) {
        return "elevated"
    }
    return "medium"
}

fun strictCameraEventCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 80741.0, in order. */
fun strictCameraEventCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80741.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
