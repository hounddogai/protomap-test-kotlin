package com.acme.transport.safety.coaching.request

import com.acme.transport.safety.cameraevent.eventscore.DefaultCameraEventScore
import com.acme.transport.safety.cameraevent.eventscore.classifyGlobalCameraEventBudget

fun summarizeDefaultCameraEventScoresForRequest(items: MutableList<DefaultCameraEventScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveSeasonalCoachingRequestsFromScore(items: MutableList<DefaultCameraEventScore>): MutableList<SeasonalCoachingRequest> {
    val out = mutableListOf<SeasonalCoachingRequest>()
    for (item in items) {
        val derived = newSeasonalCoachingRequest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleSeasonalCoachingRequest(item: SeasonalCoachingRequest): String {
    return classifyGlobalCameraEventBudget(item.effectiveTotal())
}
