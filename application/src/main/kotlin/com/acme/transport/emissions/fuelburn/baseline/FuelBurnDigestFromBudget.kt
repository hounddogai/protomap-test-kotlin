package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.emissions.carbonreport.claim.CarbonReportBudget
import com.acme.transport.emissions.carbonreport.claim.normalizeCarbonReportLedgerCode

fun summarizeCarbonReportBudgetsForDigest(items: MutableList<CarbonReportBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveFuelBurnDigestsFromBudget(items: MutableList<CarbonReportBudget>): MutableList<FuelBurnDigest> {
    val out = mutableListOf<FuelBurnDigest>()
    for (item in items) {
        val derived = newFuelBurnDigest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeFuelBurnDigest(item: FuelBurnDigest): String = normalizeCarbonReportLedgerCode(item.code)
