package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.safety.cameraevent.defaulteventscore.SeasonalCameraEventQuota
import com.acme.transport.safety.cameraevent.defaulteventscore.classifyAdaptiveCameraEventGroup

fun summarizeSeasonalCameraEventQuotasForSegment(items: MutableList<SeasonalCameraEventQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveGlobalCoachingSegmentsFromQuota(items: MutableList<SeasonalCameraEventQuota>): MutableList<GlobalCoachingSegment> {
    val out = mutableListOf<GlobalCoachingSegment>()
    for (item in items) {
        val derived = newGlobalCoachingSegment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planGlobalCoachingSegment(item: GlobalCoachingSegment): String {
    return classifyAdaptiveCameraEventGroup(item.weightedExposure())
}
