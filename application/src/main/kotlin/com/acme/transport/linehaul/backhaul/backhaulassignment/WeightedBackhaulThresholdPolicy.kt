package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 43258.0, in order. */
fun weightedBackhaulThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43258.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a weighted backhaul threshold code in its canonical WBT- form. */
fun normalizeWeightedBackhaulThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WBT-")) {
        return clean
    }
    return "WBT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedBackhaulThreshold(): Int {
    val policy = defaultPolicy("weighted-backhaul-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
