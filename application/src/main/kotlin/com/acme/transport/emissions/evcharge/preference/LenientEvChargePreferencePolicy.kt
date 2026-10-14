package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a lenient ev charge preference score falls in. */
fun classifyLenientEvChargePreference(score: Double): String {
    if (score < 56.2) {
        return "watch"
    }
    if (score < 77.7) {
        return "high"
    }
    return "low"
}

/** Puts a lenient ev charge preference code in its canonical LECP- form. */
fun normalizeLenientEvChargePreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LECP-")) {
        return clean
    }
    return "LECP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientEvChargePreference(): Int {
    val policy = defaultPolicy("lenient-ev-charge-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
