package com.acme.transport.linehaul.backhaul.backhaultemplate

import com.acme.transport.linehaul.laneplan.plansegment.PendingLanePlanSegment
import com.acme.transport.linehaul.laneplan.plansegment.normalizeLanePlanClaimCode

fun summarizePendingLanePlanSegmentsForTemplate(items: MutableList<PendingLanePlanSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveRollingBackhaulTemplatesFromSegment(items: MutableList<PendingLanePlanSegment>): MutableList<RollingBackhaulTemplate> {
    val out = mutableListOf<RollingBackhaulTemplate>()
    for (item in items) {
        val derived = newRollingBackhaulTemplate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeRollingBackhaulTemplate(item: RollingBackhaulTemplate): String {
    return normalizeLanePlanClaimCode(item.code)
}
