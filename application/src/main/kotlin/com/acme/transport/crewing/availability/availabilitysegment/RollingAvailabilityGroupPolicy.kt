package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a rolling availability group code in its canonical RAG- form. */
fun normalizeRollingAvailabilityGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RAG-")) {
        return clean
    }
    return "RAG-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingAvailabilityGroup(): Int {
    val policy = defaultPolicy("rolling-availability-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 78936.0, in order. */
fun rollingAvailabilityGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78936.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a rolling availability group score falls in. */
fun classifyRollingAvailabilityGroup(score: Double): String {
    if (score < 24.2) {
        return "elevated"
    }
    if (score < 86.8) {
        return "low"
    }
    if (score < 90.3) {
        return "ok"
    }
    return "critical"
}
