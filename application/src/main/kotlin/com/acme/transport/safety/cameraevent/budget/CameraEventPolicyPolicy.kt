package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.numbers.roundTo

/** Names the band a camera event policy score falls in. */
fun classifyCameraEventPolicy(score: Double): String {
    if (score < 18.7) {
        return "high"
    }
    if (score < 19.0) {
        return "low"
    }
    if (score < 58.2) {
        return "ok"
    }
    if (score < 82.6) {
        return "elevated"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 77872.0, in order. */
fun cameraEventPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77872.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cameraEventPolicyBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
