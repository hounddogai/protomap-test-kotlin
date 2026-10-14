package com.acme.transport.emissions.fuelburn.profile

import com.acme.transport.emissions.carbonreport.forecast.LenientCarbonReportAssignment
import com.acme.transport.emissions.carbonreport.forecast.normalizeCarbonReportProfileCode

fun summarizeLenientCarbonReportAssignmentsForProfile(items: MutableList<LenientCarbonReportAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveFuelBurnProfilesFromAssignment(items: MutableList<LenientCarbonReportAssignment>): MutableList<FuelBurnProfile> {
    val out = mutableListOf<FuelBurnProfile>()
    for (item in items) {
        val derived = newFuelBurnProfile(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleFuelBurnProfile(item: FuelBurnProfile): String = normalizeCarbonReportProfileCode(item.code)
