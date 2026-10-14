package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a adaptive locker proposal score falls in. */
fun classifyAdaptiveLockerProposal(score: Double): String {
    if (score < 33.8) {
        return "watch"
    }
    if (score < 80.1) {
        return "low"
    }
    return "medium"
}

fun mergeAdaptiveLockerProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
