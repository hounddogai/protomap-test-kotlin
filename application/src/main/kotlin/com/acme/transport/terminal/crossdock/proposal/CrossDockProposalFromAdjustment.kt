package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.terminal.dockdoor.score.DockDoorAdjustment
import com.acme.transport.terminal.dockdoor.score.classifyDockDoorNotice

fun summarizeDockDoorAdjustmentsForProposal(items: MutableList<DockDoorAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveCrossDockProposalsFromAdjustment(items: MutableList<DockDoorAdjustment>): MutableList<CrossDockProposal> {
    val out = mutableListOf<CrossDockProposal>()
    for (item in items) {
        val derived = newCrossDockProposal(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceCrossDockProposal(item: CrossDockProposal): String = classifyDockDoorNotice(item.weightedLoad())
