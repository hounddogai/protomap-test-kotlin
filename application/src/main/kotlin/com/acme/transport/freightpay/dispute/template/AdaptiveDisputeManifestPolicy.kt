package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a adaptive dispute manifest code in its canonical ADM- form. */
fun normalizeAdaptiveDisputeManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ADM-")) {
        return clean
    }
    return "ADM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveDisputeManifest(): Int {
    val policy = defaultPolicy("adaptive-dispute-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a adaptive dispute manifest score falls in. */
fun classifyAdaptiveDisputeManifest(score: Double): String {
    if (score < 1.3) {
        return "elevated"
    }
    if (score < 44.3) {
        return "low"
    }
    if (score < 91.4) {
        return "minimal"
    }
    return "ok"
}
