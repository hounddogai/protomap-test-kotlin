package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun disputeCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a dispute checkpoint code in its canonical DC- form. */
fun normalizeDisputeCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DC-")) {
        return clean
    }
    return "DC-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDisputeCheckpoint(): Int {
    val policy = defaultPolicy("dispute-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 8075.0, in order. */
fun disputeCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8075.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
