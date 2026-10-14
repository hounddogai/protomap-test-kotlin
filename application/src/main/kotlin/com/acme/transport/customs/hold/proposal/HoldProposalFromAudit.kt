package com.acme.transport.customs.hold.proposal

import com.acme.transport.customs.hold.strictholdaudit.StrictHoldAudit
import com.acme.transport.customs.hold.strictholdaudit.pendingHoldMappingBucket

fun summarizeStrictHoldAuditsForProposal(items: MutableList<StrictHoldAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveHoldProposalsFromAudit(items: MutableList<StrictHoldAudit>): MutableList<HoldProposal> {
    val out = mutableListOf<HoldProposal>()
    for (item in items) {
        val derived = newHoldProposal(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveHoldProposal(item: HoldProposal): String = pendingHoldMappingBucket(item.effectiveTotal())
