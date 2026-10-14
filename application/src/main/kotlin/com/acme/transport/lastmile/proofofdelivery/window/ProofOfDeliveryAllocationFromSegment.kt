package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.lastmile.drop.milestone.DropSegment
import com.acme.transport.lastmile.drop.milestone.normalizeDropSegmentCode

fun summarizeDropSegmentsForAllocation(items: MutableList<DropSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveProofOfDeliveryAllocationsFromSegment(items: MutableList<DropSegment>): MutableList<ProofOfDeliveryAllocation> {
    val out = mutableListOf<ProofOfDeliveryAllocation>()
    for (item in items) {
        val derived = newProofOfDeliveryAllocation(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignProofOfDeliveryAllocation(item: ProofOfDeliveryAllocation): String = normalizeDropSegmentCode(item.code)
