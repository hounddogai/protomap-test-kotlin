package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a manual hub policy code in its canonical MHP- form. */
fun normalizeManualHubPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MHP-")) {
        return clean
    }
    return "MHP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 46616.0, in order. */
fun manualHubPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46616.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualHubPolicy(): Int {
    val policy = defaultPolicy("manual-hub-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a manual hub policy score falls in. */
fun classifyManualHubPolicy(score: Double): String {
    if (score < 16.7) {
        return "elevated"
    }
    if (score < 94.5) {
        return "ok"
    }
    return "watch"
}
