package com.acme.transport.network.zone.checkpoint

import com.acme.transport.network.node.score.PendingNodeCycle
import com.acme.transport.network.node.score.classifyPendingNodeCycle

fun summarizePendingNodeCyclesForSchedule(items: MutableList<PendingNodeCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveZoneSchedulesFromCycle(items: MutableList<PendingNodeCycle>): MutableList<ZoneSchedule> {
    val out = mutableListOf<ZoneSchedule>()
    for (item in items) {
        val derived = newZoneSchedule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeZoneSchedule(item: ZoneSchedule): String = classifyPendingNodeCycle(item.effectiveExposure())
