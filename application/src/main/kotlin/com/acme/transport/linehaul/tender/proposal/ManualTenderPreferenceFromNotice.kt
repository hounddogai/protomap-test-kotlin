package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.linehaul.laneplan.planreservation.FallbackLanePlanNotice
import com.acme.transport.linehaul.laneplan.planreservation.fallbackLanePlanNoticeBucket

fun summarizeFallbackLanePlanNoticesForPreference(items: MutableList<FallbackLanePlanNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveManualTenderPreferencesFromNotice(items: MutableList<FallbackLanePlanNotice>): MutableList<ManualTenderPreference> {
    val out = mutableListOf<ManualTenderPreference>()
    for (item in items) {
        val derived = newManualTenderPreference(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeManualTenderPreference(item: ManualTenderPreference): String {
    return fallbackLanePlanNoticeBucket(item.weightedExposure())
}
