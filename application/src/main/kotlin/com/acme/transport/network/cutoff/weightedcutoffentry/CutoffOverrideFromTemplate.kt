package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.network.servicearea.manualareathreshold.RegionalServiceAreaTemplate
import com.acme.transport.network.servicearea.manualareathreshold.normalizeServiceAreaPreferenceCode

fun summarizeRegionalServiceAreaTemplatesForOverride(items: MutableList<RegionalServiceAreaTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveCutoffOverridesFromTemplate(items: MutableList<RegionalServiceAreaTemplate>): MutableList<CutoffOverride> {
    val out = mutableListOf<CutoffOverride>()
    for (item in items) {
        val derived = newCutoffOverride(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveCutoffOverride(item: CutoffOverride): String = normalizeServiceAreaPreferenceCode(item.code)
