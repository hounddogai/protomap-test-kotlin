package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedDisputePreference(): Int {
    val policy = defaultPolicy("weighted-dispute-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun weightedDisputePreferenceBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 24842.0, in order. */
fun weightedDisputePreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24842.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a weighted dispute preference code in its canonical WDP- form. */
fun normalizeWeightedDisputePreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WDP-")) {
        return clean
    }
    return "WDP-${clean.replace(" ", "-")}"
}
