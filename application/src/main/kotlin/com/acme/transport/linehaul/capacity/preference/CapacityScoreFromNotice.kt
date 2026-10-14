package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.linehaul.backhaul.assignment.BackhaulNotice
import com.acme.transport.linehaul.backhaul.assignment.classifyBackhaulAssignment

fun summarizeBackhaulNoticesForScore(items: MutableList<BackhaulNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveCapacityScoresFromNotice(items: MutableList<BackhaulNotice>): MutableList<CapacityScore> {
    val out = mutableListOf<CapacityScore>()
    for (item in items) {
        val derived = newCapacityScore(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankCapacityScore(item: CapacityScore): String = classifyBackhaulAssignment(item.weightedTotal())
