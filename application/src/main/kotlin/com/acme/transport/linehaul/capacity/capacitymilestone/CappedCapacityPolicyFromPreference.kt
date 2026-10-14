package com.acme.transport.linehaul.capacity.capacitymilestone

import com.acme.transport.linehaul.tender.preference.LegacyTenderPreference
import com.acme.transport.linehaul.tender.preference.manualTenderEstimateBucket

fun summarizeLegacyTenderPreferencesForPolicy(items: MutableList<LegacyTenderPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveCappedCapacityPoliciesFromPreference(items: MutableList<LegacyTenderPreference>): MutableList<CappedCapacityPolicy> {
    val out = mutableListOf<CappedCapacityPolicy>()
    for (item in items) {
        val derived = newCappedCapacityPolicy(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignCappedCapacityPolicy(item: CappedCapacityPolicy): String {
    return manualTenderEstimateBucket(item.effectiveVolume())
}
