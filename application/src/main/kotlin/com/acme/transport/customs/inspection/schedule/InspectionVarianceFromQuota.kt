package com.acme.transport.customs.inspection.schedule

import com.acme.transport.customs.broker.snapshot.RollingBrokerQuota
import com.acme.transport.customs.broker.snapshot.classifyBrokerSnapshot

fun summarizeRollingBrokerQuotasForVariance(items: MutableList<RollingBrokerQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveInspectionVariancesFromQuota(items: MutableList<RollingBrokerQuota>): MutableList<InspectionVariance> {
    val out = mutableListOf<InspectionVariance>()
    for (item in items) {
        val derived = newInspectionVariance(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceInspectionVariance(item: InspectionVariance): String = classifyBrokerSnapshot(item.weightedVolume())
