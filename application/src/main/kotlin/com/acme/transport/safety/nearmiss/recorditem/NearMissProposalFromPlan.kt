package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.safety.nearmiss.plan.LegacyNearMissPlan
import com.acme.transport.safety.nearmiss.plan.nearMissRequestBucket

fun summarizeLegacyNearMissPlansForProposal(items: MutableList<LegacyNearMissPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveNearMissProposalsFromPlan(items: MutableList<LegacyNearMissPlan>): MutableList<NearMissProposal> {
    val out = mutableListOf<NearMissProposal>()
    for (item in items) {
        val derived = newNearMissProposal(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeNearMissProposal(item: NearMissProposal): String = nearMissRequestBucket(item.projectedExposure())
