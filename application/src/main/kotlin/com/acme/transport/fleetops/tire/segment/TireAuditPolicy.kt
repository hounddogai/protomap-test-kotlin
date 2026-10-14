package com.acme.transport.fleetops.tire.segment

import com.acme.transport.kit.numbers.roundTo

fun tireAuditBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a tire audit score falls in. */
fun classifyTireAudit(score: Double): String {
    if (score < 26.9) {
        return "medium"
    }
    if (score < 64.2) {
        return "ok"
    }
    return "low"
}
