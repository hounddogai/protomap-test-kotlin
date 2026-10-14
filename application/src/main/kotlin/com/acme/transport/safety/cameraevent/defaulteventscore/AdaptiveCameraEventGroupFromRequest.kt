package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.safety.nearmiss.segment.GlobalNearMissRequest
import com.acme.transport.safety.nearmiss.segment.classifyAdaptiveNearMissSegment

fun summarizeGlobalNearMissRequestsForGroup(items: MutableList<GlobalNearMissRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveAdaptiveCameraEventGroupsFromRequest(items: MutableList<GlobalNearMissRequest>): MutableList<AdaptiveCameraEventGroup> {
    val out = mutableListOf<AdaptiveCameraEventGroup>()
    for (item in items) {
        val derived = newAdaptiveCameraEventGroup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitAdaptiveCameraEventGroup(item: AdaptiveCameraEventGroup): String {
    return classifyAdaptiveNearMissSegment(item.effectiveValue())
}
