package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.emissions.evcharge.baseline.EvChargeTier
import com.acme.transport.emissions.evcharge.baseline.normalizeEvChargeRequestCode

fun summarizeEvChargeTiersForGroup(items: MutableList<EvChargeTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRegionalCarbonReportGroupsFromTier(items: MutableList<EvChargeTier>): MutableList<RegionalCarbonReportGroup> {
    val out = mutableListOf<RegionalCarbonReportGroup>()
    for (item in items) {
        val derived = newRegionalCarbonReportGroup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignRegionalCarbonReportGroup(item: RegionalCarbonReportGroup): String {
    return normalizeEvChargeRequestCode(item.code)
}
