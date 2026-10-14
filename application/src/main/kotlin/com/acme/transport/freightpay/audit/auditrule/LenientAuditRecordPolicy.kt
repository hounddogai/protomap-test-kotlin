package com.acme.transport.freightpay.audit.auditrule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLenientAuditRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a lenient audit record score falls in. */
fun classifyLenientAuditRecord(score: Double): String {
    if (score < 26.1) {
        return "minimal"
    }
    if (score < 33.4) {
        return "watch"
    }
    if (score < 38.6) {
        return "ok"
    }
    if (score < 64.2) {
        return "elevated"
    }
    return "critical"
}
