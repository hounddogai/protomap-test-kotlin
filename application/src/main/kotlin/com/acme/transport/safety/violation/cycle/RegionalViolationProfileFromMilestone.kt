package com.acme.transport.safety.violation.cycle

import com.acme.transport.safety.cameraevent.budget.CameraEventMilestone
import com.acme.transport.safety.cameraevent.budget.regionalCameraEventBudgetWeight

fun summarizeCameraEventMilestonesForProfile(items: MutableList<CameraEventMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalViolationProfilesFromMilestone(items: MutableList<CameraEventMilestone>): MutableList<RegionalViolationProfile> {
    val out = mutableListOf<RegionalViolationProfile>()
    for (item in items) {
        val derived = newRegionalViolationProfile(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignRegionalViolationProfile(item: RegionalViolationProfile): Double {
    return regionalCameraEventBudgetWeight(8, true)
}
