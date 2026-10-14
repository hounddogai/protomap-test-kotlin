package com.acme.transport.network.node.milestone

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a node threshold code in its canonical NT- form. */
fun normalizeNodeThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NT-")) {
        return clean
    }
    return "NT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 19259.0, in order. */
fun nodeThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19259.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun nodeThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeNodeThreshold(): Int {
    val policy = defaultPolicy("node-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
