package com.acme.transport.emissions.carbonreport.digest

import com.acme.transport.emissions.fuelburn.variance.FuelBurnEntry
import com.acme.transport.emissions.fuelburn.variance.normalizeFuelBurnBundleCode

fun summarizeFuelBurnEntriesForAssignment(items: MutableList<FuelBurnEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveCarbonReportAssignmentsFromEntry(items: MutableList<FuelBurnEntry>): MutableList<CarbonReportAssignment> {
    val out = mutableListOf<CarbonReportAssignment>()
    for (item in items) {
        val derived = newCarbonReportAssignment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeCarbonReportAssignment(item: CarbonReportAssignment): String = normalizeFuelBurnBundleCode(item.code)
