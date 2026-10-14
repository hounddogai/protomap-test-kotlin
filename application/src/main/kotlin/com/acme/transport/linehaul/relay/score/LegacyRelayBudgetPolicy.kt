package com.acme.transport.linehaul.relay.score

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 88327.0, in order. */
fun legacyRelayBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88327.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy relay budget code in its canonical LRB- form. */
fun normalizeLegacyRelayBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRB-")) {
        return clean
    }
    return "LRB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyRelayBudget(): Int {
    val policy = defaultPolicy("legacy-relay-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
