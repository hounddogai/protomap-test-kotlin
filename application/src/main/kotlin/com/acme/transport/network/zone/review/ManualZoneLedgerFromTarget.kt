package com.acme.transport.network.zone.review

import com.acme.transport.network.hub.metric.PrimaryHubTarget
import com.acme.transport.network.hub.metric.normalizeHubMetricCode

fun summarizePrimaryHubTargetsForLedger(items: MutableList<PrimaryHubTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveManualZoneLedgersFromTarget(items: MutableList<PrimaryHubTarget>): MutableList<ManualZoneLedger> {
    val out = mutableListOf<ManualZoneLedger>()
    for (item in items) {
        val derived = newManualZoneLedger(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignManualZoneLedger(item: ManualZoneLedger): String = normalizeHubMetricCode(item.code)
