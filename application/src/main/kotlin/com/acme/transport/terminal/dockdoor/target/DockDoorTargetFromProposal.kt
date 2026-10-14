package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.terminal.dwell.variance.DwellProposal
import com.acme.transport.terminal.dwell.variance.classifyDwellVariance

fun summarizeDwellProposalsForTarget(items: MutableList<DwellProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveDockDoorTargetsFromProposal(items: MutableList<DwellProposal>): MutableList<DockDoorTarget> {
    val out = mutableListOf<DockDoorTarget>()
    for (item in items) {
        val derived = newDockDoorTarget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDockDoorTarget(item: DockDoorTarget): String = classifyDwellVariance(item.measuredValue())
