package com.acme.transport.freightpay.ratecard.cardsnapshot

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a rate card allocation code in its canonical RCA- form. */
fun normalizeRateCardAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCA-")) {
        return clean
    }
    return "RCA-${clean.replace(" ", "-")}"
}

/** Names the band a rate card allocation score falls in. */
fun classifyRateCardAllocation(score: Double): String {
    if (score < 5.5) {
        return "minimal"
    }
    if (score < 43.5) {
        return "critical"
    }
    if (score < 45.0) {
        return "low"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRateCardAllocation(): Int {
    val policy = defaultPolicy("rate-card-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
