package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a proof of delivery batch score falls in. */
fun classifyProofOfDeliveryBatch(score: Double): String {
    if (score < 37.4) {
        return "ok"
    }
    if (score < 55.8) {
        return "low"
    }
    if (score < 56.9) {
        return "high"
    }
    if (score < 77.8) {
        return "critical"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeProofOfDeliveryBatch(): Int {
    val policy = defaultPolicy("proof-of-delivery-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a proof of delivery batch code in its canonical PODB- form. */
fun normalizeProofOfDeliveryBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PODB-")) {
        return clean
    }
    return "PODB-${clean.replace(" ", "-")}"
}
