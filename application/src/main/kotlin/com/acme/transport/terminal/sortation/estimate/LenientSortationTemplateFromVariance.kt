package com.acme.transport.terminal.sortation.estimate

import com.acme.transport.terminal.crossdock.rule.PendingCrossDockVariance
import com.acme.transport.terminal.crossdock.rule.adaptiveCrossDockRuleWeight

fun summarizePendingCrossDockVariancesForTemplate(items: MutableList<PendingCrossDockVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveLenientSortationTemplatesFromVariance(items: MutableList<PendingCrossDockVariance>): MutableList<LenientSortationTemplate> {
    val out = mutableListOf<LenientSortationTemplate>()
    for (item in items) {
        val derived = newLenientSortationTemplate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLenientSortationTemplate(item: LenientSortationTemplate): Double {
    return adaptiveCrossDockRuleWeight(6, true)
}
