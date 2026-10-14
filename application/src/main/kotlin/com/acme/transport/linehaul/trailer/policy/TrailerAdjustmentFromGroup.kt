package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.linehaul.backhaul.reservation.BackhaulGroup
import com.acme.transport.linehaul.backhaul.reservation.normalizeBackhaulCycleCode

fun summarizeBackhaulGroupsForAdjustment(items: MutableList<BackhaulGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveTrailerAdjustmentsFromGroup(items: MutableList<BackhaulGroup>): MutableList<TrailerAdjustment> {
    val out = mutableListOf<TrailerAdjustment>()
    for (item in items) {
        val derived = newTrailerAdjustment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveTrailerAdjustment(item: TrailerAdjustment): String = normalizeBackhaulCycleCode(item.code)
