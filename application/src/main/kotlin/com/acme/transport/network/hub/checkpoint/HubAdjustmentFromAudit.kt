package com.acme.transport.network.hub.checkpoint

import com.acme.transport.network.cutoff.schedule.CutoffAudit
import com.acme.transport.network.cutoff.schedule.cutoffCycleWeight

fun summarizeCutoffAuditsForAdjustment(items: MutableList<CutoffAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveHubAdjustmentsFromAudit(items: MutableList<CutoffAudit>): MutableList<HubAdjustment> {
    val out = mutableListOf<HubAdjustment>()
    for (item in items) {
        val derived = newHubAdjustment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyHubAdjustment(item: HubAdjustment): Double = cutoffCycleWeight(4, true)
