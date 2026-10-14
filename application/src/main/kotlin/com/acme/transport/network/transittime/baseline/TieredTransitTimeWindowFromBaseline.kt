package com.acme.transport.network.transittime.baseline

import com.acme.transport.network.cutoff.baseline.CutoffBaseline
import com.acme.transport.network.cutoff.baseline.classifyGlobalCutoffReservation

fun summarizeCutoffBaselinesForWindow(items: MutableList<CutoffBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveTieredTransitTimeWindowsFromBaseline(items: MutableList<CutoffBaseline>): MutableList<TieredTransitTimeWindow> {
    val out = mutableListOf<TieredTransitTimeWindow>()
    for (item in items) {
        val derived = newTieredTransitTimeWindow(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyTieredTransitTimeWindowExtra(item: TieredTransitTimeWindow): String {
    return classifyGlobalCutoffReservation(item.projectedExposure())
}
