package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a scheduled near miss quota score falls in. */
fun classifyScheduledNearMissQuota(score: Double): String {
    if (score < 33.9) {
        return "minimal"
    }
    if (score < 60.7) {
        return "elevated"
    }
    return "critical"
}

/** Puts a scheduled near miss quota code in its canonical SNMQ- form. */
fun normalizeScheduledNearMissQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SNMQ-")) {
        return clean
    }
    return "SNMQ-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledNearMissQuota(): Int {
    val policy = defaultPolicy("scheduled-near-miss-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
