package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.network.cutoff.schedule.CutoffCycle
import com.acme.transport.network.cutoff.schedule.classifyCutoffCycle

fun summarizeCutoffCyclesForCycle(items: MutableList<CutoffCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveStrictZoneCyclesFromCycle(items: MutableList<CutoffCycle>): MutableList<StrictZoneCycle> {
    val out = mutableListOf<StrictZoneCycle>()
    for (item in items) {
        val derived = newStrictZoneCycle(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveStrictZoneCycle(item: StrictZoneCycle): String = classifyCutoffCycle(item.effectiveLoad())
