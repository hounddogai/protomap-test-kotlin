package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.emissions.carbonreport.adjustment.CarbonReportAdjustment
import com.acme.transport.emissions.carbonreport.adjustment.classifyWeightedCarbonReportLedger

fun summarizeCarbonReportAdjustmentsForPreference(items: MutableList<CarbonReportAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveEvChargePreferencesFromAdjustment(items: MutableList<CarbonReportAdjustment>): MutableList<EvChargePreference> {
    val out = mutableListOf<EvChargePreference>()
    for (item in items) {
        val derived = newEvChargePreference(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeEvChargePreference(item: EvChargePreference): String {
    return classifyWeightedCarbonReportLedger(item.weightedExposure())
}
