package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.kit.numbers.roundTo

/** Names the band a audit mapping score falls in. */
fun classifyAuditMapping(score: Double): String {
    if (score < 7.7) {
        return "watch"
    }
    if (score < 31.7) {
        return "high"
    }
    return "low"
}

fun auditMappingBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
