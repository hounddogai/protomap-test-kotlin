package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a rate card preference score falls in. */
fun classifyRateCardPreference(score: Double): String {
    if (score < 33.5) {
        return "high"
    }
    if (score < 94.5) {
        return "low"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRateCardPreference(): Int {
    val policy = defaultPolicy("rate-card-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a rate card preference code in its canonical RCP- form. */
fun normalizeRateCardPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCP-")) {
        return clean
    }
    return "RCP-${clean.replace(" ", "-")}"
}
