package com.acme.transport.emissions.evcharge.score

import com.acme.transport.emissions.evcharge.regionalchargeslot.EvChargeScore
import com.acme.transport.emissions.evcharge.regionalchargeslot.normalizeEvChargeScoreCode

fun summarizeEvChargeScoresForAssignment(items: MutableList<EvChargeScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveEvChargeAssignmentsFromScore(items: MutableList<EvChargeScore>): MutableList<EvChargeAssignment> {
    val out = mutableListOf<EvChargeAssignment>()
    for (item in items) {
        val derived = newEvChargeAssignment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchEvChargeAssignment(item: EvChargeAssignment): String = normalizeEvChargeScoreCode(item.code)
