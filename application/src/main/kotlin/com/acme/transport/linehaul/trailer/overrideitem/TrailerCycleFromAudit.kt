package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.linehaul.laneplan.schedule.LanePlanAudit
import com.acme.transport.linehaul.laneplan.schedule.scheduledLanePlanScheduleBucket

fun summarizeLanePlanAuditsForCycle(items: MutableList<LanePlanAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveTrailerCyclesFromAudit(items: MutableList<LanePlanAudit>): MutableList<TrailerCycle> {
    val out = mutableListOf<TrailerCycle>()
    for (item in items) {
        val derived = newTrailerCycle(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planTrailerCycle(item: TrailerCycle): String = scheduledLanePlanScheduleBucket(item.effectiveTotal())
