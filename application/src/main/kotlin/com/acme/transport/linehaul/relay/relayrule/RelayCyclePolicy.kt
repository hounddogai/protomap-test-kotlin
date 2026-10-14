package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.retry.defaultPolicy

fun relayCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRelayCycle(): Int {
    val policy = defaultPolicy("relay-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a relay cycle code in its canonical RC- form. */
fun normalizeRelayCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RC-")) {
        return clean
    }
    return "RC-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 26980.0, in order. */
fun relayCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 26980.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
