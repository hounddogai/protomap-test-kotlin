package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.terminal.traileryard.yardsummary.TieredTrailerYardException
import com.acme.transport.terminal.traileryard.yardsummary.trailerYardCycleWeight

fun summarizeTieredTrailerYardExceptionsForTemplate(items: MutableList<TieredTrailerYardException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveSortationTemplatesFromException(items: MutableList<TieredTrailerYardException>): MutableList<AdaptiveSortationTemplate> {
    val out = mutableListOf<AdaptiveSortationTemplate>()
    for (item in items) {
        val derived = newAdaptiveSortationTemplate(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleAdaptiveSortationTemplate(item: AdaptiveSortationTemplate): Double = trailerYardCycleWeight(4, true)
