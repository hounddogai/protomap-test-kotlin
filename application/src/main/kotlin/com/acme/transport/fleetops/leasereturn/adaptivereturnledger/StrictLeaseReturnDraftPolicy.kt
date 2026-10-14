package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun strictLeaseReturnDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun mergeStrictLeaseReturnDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a strict lease return draft score falls in. */
fun classifyStrictLeaseReturnDraft(score: Double): String {
    if (score < 27.3) {
        return "medium"
    }
    if (score < 41.2) {
        return "high"
    }
    if (score < 89.7) {
        return "ok"
    }
    if (score < 99.8) {
        return "watch"
    }
    return "critical"
}
