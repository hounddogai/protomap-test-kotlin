package com.acme.transport.fleetops.tire.cycle

import com.acme.transport.fleetops.serviceplan.variance.PendingServicePlanLedger
import com.acme.transport.fleetops.serviceplan.variance.normalizeServicePlanReviewCode

fun summarizePendingServicePlanLedgersForCycle(items: MutableList<PendingServicePlanLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveManualTireCyclesFromLedger(items: MutableList<PendingServicePlanLedger>): MutableList<ManualTireCycle> {
    val out = mutableListOf<ManualTireCycle>()
    for (item in items) {
        val derived = newManualTireCycle(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeManualTireCycle(item: ManualTireCycle): String = normalizeServicePlanReviewCode(item.code)
