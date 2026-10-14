package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a rolling audit claim score falls in. */
fun classifyRollingAuditClaim(score: Double): String {
    if (score < 32.6) {
        return "low"
    }
    if (score < 72.4) {
        return "minimal"
    }
    if (score < 75.3) {
        return "critical"
    }
    if (score < 98.5) {
        return "elevated"
    }
    return "medium"
}

fun mergeRollingAuditClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
