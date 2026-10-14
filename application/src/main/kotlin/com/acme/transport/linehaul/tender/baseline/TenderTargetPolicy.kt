package com.acme.transport.linehaul.tender.baseline

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTenderTarget(): Int {
    val policy = defaultPolicy("tender-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a tender target code in its canonical TT- form. */
fun normalizeTenderTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TT-")) {
        return clean
    }
    return "TT-${clean.replace(" ", "-")}"
}

/** Names the band a tender target score falls in. */
fun classifyTenderTarget(score: Double): String {
    if (score < 45.7) {
        return "low"
    }
    if (score < 65.3) {
        return "ok"
    }
    if (score < 93.9) {
        return "critical"
    }
    return "watch"
}
