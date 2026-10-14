package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.numbers.roundTo

/** Names the band a batched fuel surcharge segment score falls in. */
fun classifyBatchedFuelSurchargeSegment(score: Double): String {
    if (score < 17.9) {
        return "minimal"
    }
    if (score < 27.1) {
        return "watch"
    }
    if (score < 67.7) {
        return "critical"
    }
    if (score < 95.5) {
        return "elevated"
    }
    return "ok"
}

fun batchedFuelSurchargeSegmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a batched fuel surcharge segment code in its canonical BFSS- form. */
fun normalizeBatchedFuelSurchargeSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BFSS-")) {
        return clean
    }
    return "BFSS-${clean.replace(" ", "-")}"
}
