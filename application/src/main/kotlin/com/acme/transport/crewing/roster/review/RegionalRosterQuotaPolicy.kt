package com.acme.transport.crewing.roster.review

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalRosterQuota(): Int {
    val policy = defaultPolicy("regional-roster-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a regional roster quota score falls in. */
fun classifyRegionalRosterQuota(score: Double): String {
    if (score < 58.6) {
        return "low"
    }
    if (score < 60.0) {
        return "minimal"
    }
    if (score < 80.3) {
        return "high"
    }
    if (score < 81.3) {
        return "medium"
    }
    return "watch"
}

/** Puts a regional roster quota code in its canonical RRQ- form. */
fun normalizeRegionalRosterQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRQ-")) {
        return clean
    }
    return "RRQ-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 53258.0, in order. */
fun regionalRosterQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53258.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
