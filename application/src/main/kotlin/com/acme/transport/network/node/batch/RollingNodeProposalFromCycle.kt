package com.acme.transport.network.node.batch

import com.acme.transport.network.cutoff.schedule.CutoffCycle
import com.acme.transport.network.cutoff.schedule.cutoffCycleWeight

fun summarizeCutoffCyclesForProposal(items: MutableList<CutoffCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingNodeProposalsFromCycle(items: MutableList<CutoffCycle>): MutableList<RollingNodeProposal> {
    val out = mutableListOf<RollingNodeProposal>()
    for (item in items) {
        val derived = newRollingNodeProposal(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeRollingNodeProposal(item: RollingNodeProposal): Double = cutoffCycleWeight(7, true)
