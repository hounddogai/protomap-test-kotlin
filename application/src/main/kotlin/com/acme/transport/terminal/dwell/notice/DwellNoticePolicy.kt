package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a dwell notice score falls in. */
fun classifyDwellNotice(score: Double): String {
    if (score < 13.4) {
        return "low"
    }
    if (score < 15.0) {
        return "ok"
    }
    if (score < 41.4) {
        return "critical"
    }
    if (score < 95.4) {
        return "minimal"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDwellNotice(): Int {
    val policy = defaultPolicy("dwell-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a dwell notice code in its canonical DN- form. */
fun normalizeDwellNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DN-")) {
        return clean
    }
    return "DN-${clean.replace(" ", "-")}"
}
