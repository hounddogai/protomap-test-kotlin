package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.customs.clearance.estimate.RegionalClearanceProfile
import com.acme.transport.customs.clearance.estimate.normalizeRegionalClearanceProfileCode

fun summarizeRegionalClearanceProfilesForSlot(items: MutableList<RegionalClearanceProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveAdaptiveTariffSlotsFromProfile(items: MutableList<RegionalClearanceProfile>): MutableList<AdaptiveTariffSlot> {
    val out = mutableListOf<AdaptiveTariffSlot>()
    for (item in items) {
        val derived = newAdaptiveTariffSlot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeAdaptiveTariffSlot(item: AdaptiveTariffSlot): String {
    return normalizeRegionalClearanceProfileCode(item.code)
}
