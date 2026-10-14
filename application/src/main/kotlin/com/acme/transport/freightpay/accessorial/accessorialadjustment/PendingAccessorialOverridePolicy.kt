package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingAccessorialOverride(): Int {
    val policy = defaultPolicy("pending-accessorial-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a pending accessorial override score falls in. */
fun classifyPendingAccessorialOverride(score: Double): String {
    if (score < 37.3) {
        return "elevated"
    }
    if (score < 39.4) {
        return "high"
    }
    if (score < 51.3) {
        return "critical"
    }
    if (score < 52.4) {
        return "ok"
    }
    return "low"
}

/** Puts a pending accessorial override code in its canonical PAO- form. */
fun normalizePendingAccessorialOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAO-")) {
        return clean
    }
    return "PAO-${clean.replace(" ", "-")}"
}
