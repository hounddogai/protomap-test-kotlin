package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a batched rate card threshold code in its canonical BRCT- form. */
fun normalizeBatchedRateCardThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BRCT-")) {
        return clean
    }
    return "BRCT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedRateCardThreshold(): Int {
    val policy = defaultPolicy("batched-rate-card-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun batchedRateCardThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
