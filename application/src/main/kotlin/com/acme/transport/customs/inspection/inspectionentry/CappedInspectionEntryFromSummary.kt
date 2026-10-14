package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.customs.tariff.fallbacktariffcycle.TariffSummary
import com.acme.transport.customs.tariff.fallbacktariffcycle.normalizeAdaptiveTariffSlotCode

fun summarizeTariffSummariesForEntry(items: MutableList<TariffSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveCappedInspectionEntriesFromSummary(items: MutableList<TariffSummary>): MutableList<CappedInspectionEntry> {
    val out = mutableListOf<CappedInspectionEntry>()
    for (item in items) {
        val derived = newCappedInspectionEntry(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeCappedInspectionEntry(item: CappedInspectionEntry): String = normalizeAdaptiveTariffSlotCode(item.code)
