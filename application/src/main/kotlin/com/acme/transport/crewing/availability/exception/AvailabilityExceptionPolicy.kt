package com.acme.transport.crewing.availability.exception

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a availability exception code in its canonical AE- form. */
fun normalizeAvailabilityExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AE-")) {
        return clean
    }
    return "AE-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAvailabilityException(): Int {
    val policy = defaultPolicy("availability-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 46279.0, in order. */
fun availabilityExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46279.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
