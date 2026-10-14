package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRateCardMapping(): Int {
    val policy = defaultPolicy("rate-card-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun rateCardMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Puts a rate card mapping code in its canonical RCM- form. */
fun normalizeRateCardMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCM-")) {
        return clean
    }
    return "RCM-${clean.replace(" ", "-")}"
}
