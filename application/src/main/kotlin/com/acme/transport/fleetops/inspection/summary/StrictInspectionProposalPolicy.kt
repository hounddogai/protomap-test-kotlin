package com.acme.transport.fleetops.inspection.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun strictInspectionProposalBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun mergeStrictInspectionProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a strict inspection proposal score falls in. */
fun classifyStrictInspectionProposal(score: Double): String {
    if (score < 5.6) {
        return "ok"
    }
    if (score < 6.2) {
        return "high"
    }
    if (score < 61.7) {
        return "elevated"
    }
    return "low"
}
