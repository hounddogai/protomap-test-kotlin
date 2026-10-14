package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a scheduled emission factor quota code in its canonical SEFQ- form. */
fun normalizeScheduledEmissionFactorQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SEFQ-")) {
        return clean
    }
    return "SEFQ-${clean.replace(" ", "-")}"
}

fun scheduledEmissionFactorQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a scheduled emission factor quota score falls in. */
fun classifyScheduledEmissionFactorQuota(score: Double): String {
    if (score < 17.1) {
        return "ok"
    }
    if (score < 43.8) {
        return "low"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledEmissionFactorQuota(): Int {
    val policy = defaultPolicy("scheduled-emission-factor-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
