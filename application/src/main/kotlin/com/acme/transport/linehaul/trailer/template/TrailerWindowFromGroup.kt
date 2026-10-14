package com.acme.transport.linehaul.trailer.template

import com.acme.transport.linehaul.trailer.group.StrictTrailerGroup
import com.acme.transport.linehaul.trailer.group.strictTrailerGroupWeight

fun summarizeStrictTrailerGroupsForWindow(items: MutableList<StrictTrailerGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveTrailerWindowsFromGroup(items: MutableList<StrictTrailerGroup>): MutableList<TrailerWindow> {
    val out = mutableListOf<TrailerWindow>()
    for (item in items) {
        val derived = newTrailerWindow(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreTrailerWindow(item: TrailerWindow): Double = strictTrailerGroupWeight(5, true)
