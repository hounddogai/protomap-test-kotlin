package com.acme.transport.linehaul.backhaul.mapping

import com.acme.transport.linehaul.backhaul.primarybackhaulvariance.CappedBackhaulVariance
import com.acme.transport.linehaul.backhaul.primarybackhaulvariance.normalizeCappedBackhaulVarianceCode

fun summarizeCappedBackhaulVariancesForWindow(items: MutableList<CappedBackhaulVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveDefaultBackhaulWindowsFromVariance(items: MutableList<CappedBackhaulVariance>): MutableList<DefaultBackhaulWindow> {
    val out = mutableListOf<DefaultBackhaulWindow>()
    for (item in items) {
        val derived = newDefaultBackhaulWindow(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyDefaultBackhaulWindowExtra(item: DefaultBackhaulWindow): String {
    return normalizeCappedBackhaulVarianceCode(item.code)
}
