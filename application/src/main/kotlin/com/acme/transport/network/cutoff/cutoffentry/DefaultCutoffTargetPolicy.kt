package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 29233.0, in order. */
fun defaultCutoffTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 29233.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultCutoffTarget(): Int {
    val policy = defaultPolicy("default-cutoff-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a default cutoff target code in its canonical DCT- form. */
fun normalizeDefaultCutoffTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DCT-")) {
        return clean
    }
    return "DCT-${clean.replace(" ", "-")}"
}
