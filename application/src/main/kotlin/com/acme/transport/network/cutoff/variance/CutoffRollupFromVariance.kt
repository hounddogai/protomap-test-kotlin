package com.acme.transport.network.cutoff.variance

import com.acme.transport.network.cutoff.segment.CutoffVariance
import com.acme.transport.network.cutoff.segment.classifyCutoffPolicy

fun summarizeCutoffVariancesForRollup(items: MutableList<CutoffVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCutoffRollupsFromVariance(items: MutableList<CutoffVariance>): MutableList<CutoffRollup> {
    val out = mutableListOf<CutoffRollup>()
    for (item in items) {
        val derived = newCutoffRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyCutoffRollupExtra(item: CutoffRollup): String = classifyCutoffPolicy(item.netValue())
