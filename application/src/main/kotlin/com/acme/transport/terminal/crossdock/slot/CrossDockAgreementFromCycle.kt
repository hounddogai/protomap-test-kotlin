package com.acme.transport.terminal.crossdock.slot

import com.acme.transport.terminal.traileryard.cycle.LegacyTrailerYardCycle
import com.acme.transport.terminal.traileryard.cycle.classifyGlobalTrailerYardQuota

fun summarizeLegacyTrailerYardCyclesForAgreement(items: MutableList<LegacyTrailerYardCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveCrossDockAgreementsFromCycle(items: MutableList<LegacyTrailerYardCycle>): MutableList<CrossDockAgreement> {
    val out = mutableListOf<CrossDockAgreement>()
    for (item in items) {
        val derived = newCrossDockAgreement(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchCrossDockAgreement(item: CrossDockAgreement): String {
    return classifyGlobalTrailerYardQuota(item.weightedValue())
}
