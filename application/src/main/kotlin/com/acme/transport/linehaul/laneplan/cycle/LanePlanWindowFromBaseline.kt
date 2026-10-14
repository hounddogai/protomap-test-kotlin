package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.linehaul.backhaul.backhaulbatch.BackhaulBaseline
import com.acme.transport.linehaul.backhaul.backhaulbatch.manualBackhaulBatchWeight

fun summarizeBackhaulBaselinesForWindow(items: MutableList<BackhaulBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveLanePlanWindowsFromBaseline(items: MutableList<BackhaulBaseline>): MutableList<LanePlanWindow> {
    val out = mutableListOf<LanePlanWindow>()
    for (item in items) {
        val derived = newLanePlanWindow(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeLanePlanWindow(item: LanePlanWindow): Double = manualBackhaulBatchWeight(7, true)
