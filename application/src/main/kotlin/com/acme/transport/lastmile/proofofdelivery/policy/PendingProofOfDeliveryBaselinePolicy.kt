package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingProofOfDeliveryBaseline(): Int {
    val policy = defaultPolicy("pending-proof-of-delivery-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a pending proof of delivery baseline score falls in. */
fun classifyPendingProofOfDeliveryBaseline(score: Double): String {
    if (score < 53.9) {
        return "medium"
    }
    if (score < 57.4) {
        return "minimal"
    }
    if (score < 65.6) {
        return "elevated"
    }
    return "low"
}

fun pendingProofOfDeliveryBaselineBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a pending proof of delivery baseline code in its canonical PPODB- form. */
fun normalizePendingProofOfDeliveryBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PPODB-")) {
        return clean
    }
    return "PPODB-${clean.replace(" ", "-")}"
}
