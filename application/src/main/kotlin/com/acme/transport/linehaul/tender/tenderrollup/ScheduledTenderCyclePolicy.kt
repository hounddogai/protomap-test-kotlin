package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledTenderCycle(): Int {
    val policy = defaultPolicy("scheduled-tender-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 12713.0, in order. */
fun scheduledTenderCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12713.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledTenderCycleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a scheduled tender cycle score falls in. */
fun classifyScheduledTenderCycle(score: Double): String {
    if (score < 31.8) {
        return "high"
    }
    if (score < 50.1) {
        return "critical"
    }
    if (score < 76.4) {
        return "elevated"
    }
    if (score < 91.3) {
        return "minimal"
    }
    return "medium"
}
