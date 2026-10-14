package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.crewing.swap.request.ScheduledSwapRequest
import com.acme.transport.crewing.swap.request.classifyStrictSwapRequest

fun summarizeScheduledSwapRequestsForPreference(items: MutableList<ScheduledSwapRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveGlobalQualificationPreferencesFromRequest(items: MutableList<ScheduledSwapRequest>): MutableList<GlobalQualificationPreference> {
    val out = mutableListOf<GlobalQualificationPreference>()
    for (item in items) {
        val derived = newGlobalQualificationPreference(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileGlobalQualificationPreference(item: GlobalQualificationPreference): String {
    return classifyStrictSwapRequest(item.measuredValue())
}
