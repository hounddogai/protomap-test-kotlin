package com.acme.transport.customs.inspection.entry

import com.acme.transport.customs.tariff.schedule.StrictTariffPolicy
import com.acme.transport.customs.tariff.schedule.strictTariffPolicyWeight

fun summarizeStrictTariffPoliciesForProfile(items: MutableList<StrictTariffPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveInspectionProfilesFromPolicy(items: MutableList<StrictTariffPolicy>): MutableList<AdaptiveInspectionProfile> {
    val out = mutableListOf<AdaptiveInspectionProfile>()
    for (item in items) {
        val derived = newAdaptiveInspectionProfile(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveAdaptiveInspectionProfile(item: AdaptiveInspectionProfile): Double = strictTariffPolicyWeight(7, true)
