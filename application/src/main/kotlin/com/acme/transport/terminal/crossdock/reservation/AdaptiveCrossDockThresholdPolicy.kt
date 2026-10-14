package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveCrossDockThreshold(): Int {
    val policy = defaultPolicy("adaptive-cross-dock-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a adaptive cross dock threshold code in its canonical ACDT- form. */
fun normalizeAdaptiveCrossDockThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ACDT-")) {
        return clean
    }
    return "ACDT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 1036.0, in order. */
fun adaptiveCrossDockThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 1036.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
