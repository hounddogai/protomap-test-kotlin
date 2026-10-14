package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 21453.0, in order. */
fun scheduledCameraEventEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21453.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a scheduled camera event estimate code in its canonical SCEE- form. */
fun normalizeScheduledCameraEventEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCEE-")) {
        return clean
    }
    return "SCEE-${clean.replace(" ", "-")}"
}

fun scheduledCameraEventEstimateBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledCameraEventEstimate(): Int {
    val policy = defaultPolicy("scheduled-camera-event-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
