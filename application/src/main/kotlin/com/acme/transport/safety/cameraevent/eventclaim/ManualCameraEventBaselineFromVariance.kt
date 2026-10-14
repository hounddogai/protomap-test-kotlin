package com.acme.transport.safety.cameraevent.eventclaim

import com.acme.transport.safety.coaching.coachingsegment.SeasonalCoachingVariance
import com.acme.transport.safety.coaching.coachingsegment.normalizeGlobalCoachingSegmentCode

fun summarizeSeasonalCoachingVariancesForBaseline(items: MutableList<SeasonalCoachingVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveManualCameraEventBaselinesFromVariance(items: MutableList<SeasonalCoachingVariance>): MutableList<ManualCameraEventBaseline> {
    val out = mutableListOf<ManualCameraEventBaseline>()
    for (item in items) {
        val derived = newManualCameraEventBaseline(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceManualCameraEventBaseline(item: ManualCameraEventBaseline): String {
    return normalizeGlobalCoachingSegmentCode(item.code)
}
