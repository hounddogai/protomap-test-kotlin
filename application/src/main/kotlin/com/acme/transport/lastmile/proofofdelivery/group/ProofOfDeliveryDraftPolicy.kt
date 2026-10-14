package com.acme.transport.lastmile.proofofdelivery.group

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a proof of delivery draft code in its canonical PODD- form. */
fun normalizeProofOfDeliveryDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PODD-")) {
        return clean
    }
    return "PODD-${clean.replace(" ", "-")}"
}

fun proofOfDeliveryDraftBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun proofOfDeliveryDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeProofOfDeliveryDraft(): Int {
    val policy = defaultPolicy("proof-of-delivery-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
