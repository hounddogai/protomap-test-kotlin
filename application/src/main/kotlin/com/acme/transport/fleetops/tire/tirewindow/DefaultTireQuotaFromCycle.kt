package com.acme.transport.fleetops.tire.tirewindow

import com.acme.transport.fleetops.tire.entry.TireCycle
import com.acme.transport.fleetops.tire.entry.tireCycleWeight

fun summarizeTireCyclesForQuota(items: MutableList<TireCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDefaultTireQuotasFromCycle(items: MutableList<TireCycle>): MutableList<DefaultTireQuota> {
    val out = mutableListOf<DefaultTireQuota>()
    for (item in items) {
        val derived = newDefaultTireQuota(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceDefaultTireQuota(item: DefaultTireQuota): Double = tireCycleWeight(1, true)
