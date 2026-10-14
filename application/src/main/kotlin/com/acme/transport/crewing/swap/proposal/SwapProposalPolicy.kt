package com.acme.transport.crewing.swap.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeSwapProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun swapProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a swap proposal score falls in. */
fun classifySwapProposal(score: Double): String {
    if (score < 9.3) {
        return "watch"
    }
    if (score < 9.8) {
        return "critical"
    }
    if (score < 77.8) {
        return "minimal"
    }
    if (score < 96.1) {
        return "elevated"
    }
    return "ok"
}
