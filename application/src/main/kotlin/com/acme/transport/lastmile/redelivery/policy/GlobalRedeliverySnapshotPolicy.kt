package com.acme.transport.lastmile.redelivery.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeGlobalRedeliverySnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a global redelivery snapshot score falls in. */
fun classifyGlobalRedeliverySnapshot(score: Double): String {
    if (score < 51.5) {
        return "ok"
    }
    if (score < 64.3) {
        return "low"
    }
    if (score < 86.9) {
        return "critical"
    }
    return "watch"
}
