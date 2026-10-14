package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.safety.coaching.request.SeasonalCoachingRequest
import com.acme.transport.safety.coaching.request.normalizeBatchedCoachingWindowCode

fun summarizeSeasonalCoachingRequestsForBaseline(items: MutableList<SeasonalCoachingRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveGlobalCoachingBaselinesFromRequest(items: MutableList<SeasonalCoachingRequest>): MutableList<GlobalCoachingBaseline> {
    val out = mutableListOf<GlobalCoachingBaseline>()
    for (item in items) {
        val derived = newGlobalCoachingBaseline(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyGlobalCoachingBaseline(item: GlobalCoachingBaseline): String {
    return normalizeBatchedCoachingWindowCode(item.code)
}
