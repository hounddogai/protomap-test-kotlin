package com.acme.transport.network.zone.draft

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a global zone target score falls in. */
fun classifyGlobalZoneTarget(score: Double): String {
    if (score < 5.5) {
        return "critical"
    }
    if (score < 58.4) {
        return "elevated"
    }
    if (score < 77.2) {
        return "low"
    }
    if (score < 80.7) {
        return "high"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalZoneTarget(): Int {
    val policy = defaultPolicy("global-zone-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 24752.0, in order. */
fun globalZoneTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24752.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a global zone target code in its canonical GZT- form. */
fun normalizeGlobalZoneTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GZT-")) {
        return clean
    }
    return "GZT-${clean.replace(" ", "-")}"
}
