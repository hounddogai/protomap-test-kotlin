package com.acme.transport.safety.nearmiss.adjustment

import com.acme.transport.safety.violation.ledger.ViolationProposal
import com.acme.transport.safety.violation.ledger.violationProposalBucket

fun summarizeViolationProposalsForAllocation(items: MutableList<ViolationProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveNearMissAllocationsFromProposal(items: MutableList<ViolationProposal>): MutableList<NearMissAllocation> {
    val out = mutableListOf<NearMissAllocation>()
    for (item in items) {
        val derived = newNearMissAllocation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileNearMissAllocation(item: NearMissAllocation): String = violationProposalBucket(item.measuredValue())
