package com.acme.transport.emissions.carbonreport.batch

import com.acme.transport.emissions.fuelburn.proposal.LenientFuelBurnProfile
import com.acme.transport.emissions.fuelburn.proposal.manualFuelBurnDigestWeight

fun summarizeLenientFuelBurnProfilesForVariance(items: MutableList<LenientFuelBurnProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveRollingCarbonReportVariancesFromProfile(items: MutableList<LenientFuelBurnProfile>): MutableList<RollingCarbonReportVariance> {
    val out = mutableListOf<RollingCarbonReportVariance>()
    for (item in items) {
        val derived = newRollingCarbonReportVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRollingCarbonReportVariance(item: RollingCarbonReportVariance): Double {
    return manualFuelBurnDigestWeight(6, true)
}
