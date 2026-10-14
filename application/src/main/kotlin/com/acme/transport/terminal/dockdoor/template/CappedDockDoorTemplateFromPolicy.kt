package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.terminal.dwell.snapshot.LenientDwellPolicy
import com.acme.transport.terminal.dwell.snapshot.classifyLenientDwellPolicy

fun summarizeLenientDwellPoliciesForTemplate(items: MutableList<LenientDwellPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveCappedDockDoorTemplatesFromPolicy(items: MutableList<LenientDwellPolicy>): MutableList<CappedDockDoorTemplate> {
    val out = mutableListOf<CappedDockDoorTemplate>()
    for (item in items) {
        val derived = newCappedDockDoorTemplate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeCappedDockDoorTemplate(item: CappedDockDoorTemplate): String {
    return classifyLenientDwellPolicy(item.weightedLoad())
}
