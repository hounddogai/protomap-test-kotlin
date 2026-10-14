package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a capped proof of delivery notice code in its canonical CPODN- form. */
fun normalizeCappedProofOfDeliveryNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CPODN-")) {
        return clean
    }
    return "CPODN-${clean.replace(" ", "-")}"
}

/** Names the band a capped proof of delivery notice score falls in. */
fun classifyCappedProofOfDeliveryNotice(score: Double): String {
    if (score < 35.7) {
        return "minimal"
    }
    if (score < 93.9) {
        return "critical"
    }
    return "ok"
}

fun cappedProofOfDeliveryNoticeBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedProofOfDeliveryNotice(): Int {
    val policy = defaultPolicy("capped-proof-of-delivery-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
