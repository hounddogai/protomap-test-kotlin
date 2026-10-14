package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.terminal.dockdoor.target.DockDoorTarget
import com.acme.transport.terminal.dockdoor.target.dockDoorClaimWeight

fun summarizeDockDoorTargetsForNotice(items: MutableList<DockDoorTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCrossDockNoticesFromTarget(items: MutableList<DockDoorTarget>): MutableList<CrossDockNotice> {
    val out = mutableListOf<CrossDockNotice>()
    for (item in items) {
        val derived = newCrossDockNotice(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceCrossDockNotice(item: CrossDockNotice): Double = dockDoorClaimWeight(5, true)
