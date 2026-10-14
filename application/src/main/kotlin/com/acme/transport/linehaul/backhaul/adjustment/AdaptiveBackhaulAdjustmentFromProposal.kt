package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.linehaul.tender.window.GlobalTenderProposal
import com.acme.transport.linehaul.tender.window.globalTenderProposalBucket

fun summarizeGlobalTenderProposalsForAdjustment(items: MutableList<GlobalTenderProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveAdaptiveBackhaulAdjustmentsFromProposal(items: MutableList<GlobalTenderProposal>): MutableList<AdaptiveBackhaulAdjustment> {
    val out = mutableListOf<AdaptiveBackhaulAdjustment>()
    for (item in items) {
        val derived = newAdaptiveBackhaulAdjustment(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyAdaptiveBackhaulAdjustment(item: AdaptiveBackhaulAdjustment): String {
    return globalTenderProposalBucket(item.projectedTotal())
}
