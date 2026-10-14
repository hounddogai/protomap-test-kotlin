package com.acme.transport.safety.coaching.ledger

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a rolling coaching preference code in its canonical RCP- form. */
fun normalizeRollingCoachingPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCP-")) {
        return clean
    }
    return "RCP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 6006.0, in order. */
fun rollingCoachingPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6006.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingCoachingPreference(): Int {
    val policy = defaultPolicy("rolling-coaching-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
