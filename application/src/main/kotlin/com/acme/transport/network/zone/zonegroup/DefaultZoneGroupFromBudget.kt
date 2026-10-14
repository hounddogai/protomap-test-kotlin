package com.acme.transport.network.zone.zonegroup

import com.acme.transport.network.cutoff.proposal.CutoffBudget
import com.acme.transport.network.cutoff.proposal.cutoffProposalBucket

fun summarizeCutoffBudgetsForGroup(items: MutableList<CutoffBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveDefaultZoneGroupsFromBudget(items: MutableList<CutoffBudget>): MutableList<DefaultZoneGroup> {
    val out = mutableListOf<DefaultZoneGroup>()
    for (item in items) {
        val derived = newDefaultZoneGroup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDefaultZoneGroup(item: DefaultZoneGroup): String = cutoffProposalBucket(item.projectedTotal())
