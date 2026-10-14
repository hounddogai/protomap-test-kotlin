package com.acme.transport.terminal.traileryard.yardquota

import com.acme.transport.terminal.crossdock.dockproposal.PendingCrossDockNotice
import com.acme.transport.terminal.crossdock.dockproposal.globalCrossDockVarianceWeight

fun summarizePendingCrossDockNoticesForRollup(items: MutableList<PendingCrossDockNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveCappedTrailerYardRollupsFromNotice(items: MutableList<PendingCrossDockNotice>): MutableList<CappedTrailerYardRollup> {
    val out = mutableListOf<CappedTrailerYardRollup>()
    for (item in items) {
        val derived = newCappedTrailerYardRollup(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateCappedTrailerYardRollup(item: CappedTrailerYardRollup): Double {
    return globalCrossDockVarianceWeight(2, true)
}
