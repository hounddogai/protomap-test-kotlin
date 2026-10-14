package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.terminal.sortation.strictsortationdigest.SortationReservation
import com.acme.transport.terminal.sortation.strictsortationdigest.sortationQuotaWeight

fun summarizeSortationReservationsForMapping(items: MutableList<SortationReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveAdaptiveTrailerYardMappingsFromReservation(items: MutableList<SortationReservation>): MutableList<AdaptiveTrailerYardMapping> {
    val out = mutableListOf<AdaptiveTrailerYardMapping>()
    for (item in items) {
        val derived = newAdaptiveTrailerYardMapping(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeAdaptiveTrailerYardMapping(item: AdaptiveTrailerYardMapping): Double = sortationQuotaWeight(8, true)
