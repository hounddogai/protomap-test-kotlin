package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.terminal.dwell.variance.DwellVariance
import com.acme.transport.terminal.dwell.variance.classifyDwellVariance

fun summarizeDwellVariancesForBaseline(items: MutableList<DwellVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveDwellBaselinesFromVariance(items: MutableList<DwellVariance>): MutableList<DwellBaseline> {
    val out = mutableListOf<DwellBaseline>()
    for (item in items) {
        val derived = newDwellBaseline(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planDwellBaseline(item: DwellBaseline): String = classifyDwellVariance(item.projectedTotal())
