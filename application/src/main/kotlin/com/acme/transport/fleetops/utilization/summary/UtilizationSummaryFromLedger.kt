package com.acme.transport.fleetops.utilization.summary

import com.acme.transport.fleetops.serviceplan.ledger.PrimaryServicePlanLedger
import com.acme.transport.fleetops.serviceplan.ledger.normalizeAdaptiveServicePlanMappingCode

fun summarizePrimaryServicePlanLedgersForSummary(items: MutableList<PrimaryServicePlanLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveUtilizationSummariesFromLedger(items: MutableList<PrimaryServicePlanLedger>): MutableList<UtilizationSummary> {
    val out = mutableListOf<UtilizationSummary>()
    for (item in items) {
        val derived = newUtilizationSummary(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreUtilizationSummary(item: UtilizationSummary): String = normalizeAdaptiveServicePlanMappingCode(item.code)
