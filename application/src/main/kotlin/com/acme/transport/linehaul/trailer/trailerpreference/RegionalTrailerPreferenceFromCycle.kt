package com.acme.transport.linehaul.trailer.trailerpreference

import com.acme.transport.linehaul.tender.assignment.TenderCycle
import com.acme.transport.linehaul.tender.assignment.classifyManualTenderSnapshot

fun summarizeTenderCyclesForPreference(items: MutableList<TenderCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveRegionalTrailerPreferencesFromCycle(items: MutableList<TenderCycle>): MutableList<RegionalTrailerPreference> {
    val out = mutableListOf<RegionalTrailerPreference>()
    for (item in items) {
        val derived = newRegionalTrailerPreference(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planRegionalTrailerPreference(item: RegionalTrailerPreference): String {
    return classifyManualTenderSnapshot(item.netValue())
}
