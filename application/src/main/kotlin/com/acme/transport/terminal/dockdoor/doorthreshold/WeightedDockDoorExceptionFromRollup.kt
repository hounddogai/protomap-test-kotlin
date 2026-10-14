package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.terminal.traileryard.yardquota.CappedTrailerYardRollup
import com.acme.transport.terminal.traileryard.yardquota.classifyBatchedTrailerYardQuota

fun summarizeCappedTrailerYardRollupsForException(items: MutableList<CappedTrailerYardRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveWeightedDockDoorExceptionsFromRollup(items: MutableList<CappedTrailerYardRollup>): MutableList<WeightedDockDoorException> {
    val out = mutableListOf<WeightedDockDoorException>()
    for (item in items) {
        val derived = newWeightedDockDoorException(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeWeightedDockDoorException(item: WeightedDockDoorException): String {
    return classifyBatchedTrailerYardQuota(item.netVolume())
}
