package com.acme.transport.freightpay.ratecard.entry

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a adaptive rate card template code in its canonical ARCT- form. */
fun normalizeAdaptiveRateCardTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ARCT-")) {
        return clean
    }
    return "ARCT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveRateCardTemplate(): Int {
    val policy = defaultPolicy("adaptive-rate-card-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
