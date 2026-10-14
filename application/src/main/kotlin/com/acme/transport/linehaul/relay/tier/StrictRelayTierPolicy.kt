package com.acme.transport.linehaul.relay.tier

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 55378.0, in order. */
fun strictRelayTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 55378.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictRelayTier(): Int {
    val policy = defaultPolicy("strict-relay-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a strict relay tier code in its canonical SRT- form. */
fun normalizeStrictRelayTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRT-")) {
        return clean
    }
    return "SRT-${clean.replace(" ", "-")}"
}
