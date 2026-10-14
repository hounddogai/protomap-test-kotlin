package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 14350.0, in order. */
fun globalAccessorialAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 14350.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a global accessorial adjustment score falls in. */
fun classifyGlobalAccessorialAdjustment(score: Double): String {
    if (score < 37.0) {
        return "low"
    }
    if (score < 60.1) {
        return "medium"
    }
    if (score < 93.8) {
        return "high"
    }
    return "elevated"
}

/** Puts a global accessorial adjustment code in its canonical GAA- form. */
fun normalizeGlobalAccessorialAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GAA-")) {
        return clean
    }
    return "GAA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalAccessorialAdjustment(): Int {
    val policy = defaultPolicy("global-accessorial-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
