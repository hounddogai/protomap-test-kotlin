package com.acme.transport.customs.broker.brokerestimate

import com.acme.transport.customs.clearance.slot.ClearanceSlot
import com.acme.transport.customs.clearance.slot.pendingClearanceRecordWeight

fun summarizeClearanceSlotsForEstimate(items: MutableList<ClearanceSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveSeasonalBrokerEstimatesFromSlot(items: MutableList<ClearanceSlot>): MutableList<SeasonalBrokerEstimate> {
    val out = mutableListOf<SeasonalBrokerEstimate>()
    for (item in items) {
        val derived = newSeasonalBrokerEstimate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeSeasonalBrokerEstimate(item: SeasonalBrokerEstimate): Double = pendingClearanceRecordWeight(9, true)
