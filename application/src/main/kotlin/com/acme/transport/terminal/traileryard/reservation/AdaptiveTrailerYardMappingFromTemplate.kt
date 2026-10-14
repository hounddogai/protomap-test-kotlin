package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.terminal.dwell.review.ScheduledDwellTemplate
import com.acme.transport.terminal.dwell.review.dwellDraftWeight

fun summarizeScheduledDwellTemplatesForMapping(items: MutableList<ScheduledDwellTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveAdaptiveTrailerYardMappingsFromTemplate(items: MutableList<ScheduledDwellTemplate>): MutableList<AdaptiveTrailerYardMapping> {
    val out = mutableListOf<AdaptiveTrailerYardMapping>()
    for (item in items) {
        val derived = newAdaptiveTrailerYardMapping(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateAdaptiveTrailerYardMapping(item: AdaptiveTrailerYardMapping): Double = dwellDraftWeight(1, true)
