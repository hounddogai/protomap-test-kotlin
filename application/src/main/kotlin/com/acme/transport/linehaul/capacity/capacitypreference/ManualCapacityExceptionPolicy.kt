package com.acme.transport.linehaul.capacity.capacitypreference

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualCapacityException(): Int {
    val policy = defaultPolicy("manual-capacity-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a manual capacity exception code in its canonical MCE- form. */
fun normalizeManualCapacityExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MCE-")) {
        return clean
    }
    return "MCE-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 53667.0, in order. */
fun manualCapacityExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53667.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
