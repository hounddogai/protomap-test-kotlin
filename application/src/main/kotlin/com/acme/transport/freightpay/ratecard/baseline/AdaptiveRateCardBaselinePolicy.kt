package com.acme.transport.freightpay.ratecard.baseline

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a adaptive rate card baseline code in its canonical ARCB- form. */
fun normalizeAdaptiveRateCardBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ARCB-")) {
        return clean
    }
    return "ARCB-${clean.replace(" ", "-")}"
}

fun adaptiveRateCardBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveRateCardBaseline(): Int {
    val policy = defaultPolicy("adaptive-rate-card-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
