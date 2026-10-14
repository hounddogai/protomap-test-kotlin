package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.customs.tariff.cycle.TariffAudit
import com.acme.transport.customs.tariff.cycle.tariffCycleWeight

fun summarizeTariffAuditsForMetric(items: MutableList<TariffAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveHoldMetricsFromAudit(items: MutableList<TariffAudit>): MutableList<HoldMetric> {
    val out = mutableListOf<HoldMetric>()
    for (item in items) {
        val derived = newHoldMetric(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitHoldMetric(item: HoldMetric): Double = tariffCycleWeight(2, true)
