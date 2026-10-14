package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.terminal.traileryard.target.LegacyTrailerYardAgreement
import com.acme.transport.terminal.traileryard.target.rollingTrailerYardReviewWeight

fun summarizeLegacyTrailerYardAgreementsForBatch(items: MutableList<LegacyTrailerYardAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDockDoorBatchesFromAgreement(items: MutableList<LegacyTrailerYardAgreement>): MutableList<DockDoorBatch> {
    val out = mutableListOf<DockDoorBatch>()
    for (item in items) {
        val derived = newDockDoorBatch(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileDockDoorBatch(item: DockDoorBatch): Double = rollingTrailerYardReviewWeight(6, true)
