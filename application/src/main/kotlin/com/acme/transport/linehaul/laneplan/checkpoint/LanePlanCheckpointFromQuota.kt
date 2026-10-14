package com.acme.transport.linehaul.laneplan.checkpoint

import com.acme.transport.linehaul.backhaul.reservation.BackhaulQuota
import com.acme.transport.linehaul.backhaul.reservation.classifyBackhaulGroup

fun summarizeBackhaulQuotasForCheckpoint(items: MutableList<BackhaulQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveLanePlanCheckpointsFromQuota(items: MutableList<BackhaulQuota>): MutableList<LanePlanCheckpoint> {
    val out = mutableListOf<LanePlanCheckpoint>()
    for (item in items) {
        val derived = newLanePlanCheckpoint(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeLanePlanCheckpoint(item: LanePlanCheckpoint): String = classifyBackhaulGroup(item.weightedValue())
