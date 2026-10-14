package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.linehaul.backhaul.mapping.DefaultBackhaulWindow
import com.acme.transport.linehaul.backhaul.mapping.classifyStrictBackhaulMapping

fun summarizeDefaultBackhaulWindowsForTarget(items: MutableList<DefaultBackhaulWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveDefaultCapacityTargetsFromWindow(items: MutableList<DefaultBackhaulWindow>): MutableList<DefaultCapacityTarget> {
    val out = mutableListOf<DefaultCapacityTarget>()
    for (item in items) {
        val derived = newDefaultCapacityTarget(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileDefaultCapacityTarget(item: DefaultCapacityTarget): String {
    return classifyStrictBackhaulMapping(item.measuredValue())
}
