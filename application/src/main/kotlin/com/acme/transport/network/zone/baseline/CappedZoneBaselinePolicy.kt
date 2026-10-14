package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedZoneBaseline(): Int {
    val policy = defaultPolicy("capped-zone-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a capped zone baseline code in its canonical CZB- form. */
fun normalizeCappedZoneBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CZB-")) {
        return clean
    }
    return "CZB-${clean.replace(" ", "-")}"
}

/** Names the band a capped zone baseline score falls in. */
fun classifyCappedZoneBaseline(score: Double): String {
    if (score < 45.2) {
        return "ok"
    }
    if (score < 56.5) {
        return "critical"
    }
    if (score < 65.9) {
        return "minimal"
    }
    return "elevated"
}
