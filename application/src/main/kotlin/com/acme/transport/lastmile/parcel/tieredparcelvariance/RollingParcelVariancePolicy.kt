package com.acme.transport.lastmile.parcel.tieredparcelvariance

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 83021.0, in order. */
fun rollingParcelVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83021.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingParcelVariance(): Int {
    val policy = defaultPolicy("rolling-parcel-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a rolling parcel variance code in its canonical RPV- form. */
fun normalizeRollingParcelVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RPV-")) {
        return clean
    }
    return "RPV-${clean.replace(" ", "-")}"
}

/** Names the band a rolling parcel variance score falls in. */
fun classifyRollingParcelVariance(score: Double): String {
    if (score < 8.1) {
        return "ok"
    }
    if (score < 8.9) {
        return "high"
    }
    if (score < 27.2) {
        return "minimal"
    }
    if (score < 67.1) {
        return "low"
    }
    return "critical"
}
