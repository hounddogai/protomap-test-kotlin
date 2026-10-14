package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeInspectionProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a inspection proposal score falls in. */
fun classifyInspectionProposal(score: Double): String {
    if (score < 6.5) {
        return "low"
    }
    if (score < 9.7) {
        return "elevated"
    }
    if (score < 67.7) {
        return "critical"
    }
    return "ok"
}

fun inspectionProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
