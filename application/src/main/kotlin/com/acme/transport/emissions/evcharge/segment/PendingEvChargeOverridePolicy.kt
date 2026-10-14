package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a pending ev charge override score falls in. */
fun classifyPendingEvChargeOverride(score: Double): String {
    if (score < 30.4) {
        return "minimal"
    }
    if (score < 80.0) {
        return "high"
    }
    return "ok"
}

/** Puts a pending ev charge override code in its canonical PECO- form. */
fun normalizePendingEvChargeOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PECO-")) {
        return clean
    }
    return "PECO-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingEvChargeOverride(): Int {
    val policy = defaultPolicy("pending-ev-charge-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
