package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyLanePlanQuota(): Int {
    val policy = defaultPolicy("legacy-lane-plan-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun legacyLanePlanQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a legacy lane plan quota code in its canonical LLPQ- form. */
fun normalizeLegacyLanePlanQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LLPQ-")) {
        return clean
    }
    return "LLPQ-${clean.replace(" ", "-")}"
}

/** Names the band a legacy lane plan quota score falls in. */
fun classifyLegacyLanePlanQuota(score: Double): String {
    if (score < 20.5) {
        return "low"
    }
    if (score < 24.8) {
        return "medium"
    }
    return "minimal"
}
