package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.customs.tariff.milestone.AdaptiveTariffMilestone
import com.acme.transport.customs.tariff.milestone.normalizeAdaptiveTariffMilestoneCode

fun summarizeAdaptiveTariffMilestonesForWindow(items: MutableList<AdaptiveTariffMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveManualHoldWindowsFromMilestone(items: MutableList<AdaptiveTariffMilestone>): MutableList<ManualHoldWindow> {
    val out = mutableListOf<ManualHoldWindow>()
    for (item in items) {
        val derived = newManualHoldWindow(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankManualHoldWindow(item: ManualHoldWindow): String = normalizeAdaptiveTariffMilestoneCode(item.code)
