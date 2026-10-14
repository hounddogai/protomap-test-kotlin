package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.terminal.sortation.forecast.AdaptiveSortationTemplate
import com.acme.transport.terminal.sortation.forecast.classifySortationAdjustment

fun summarizeAdaptiveSortationTemplatesForRecord(items: MutableList<AdaptiveSortationTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveTrailerYardRecordsFromTemplate(items: MutableList<AdaptiveSortationTemplate>): MutableList<TrailerYardRecord> {
    val out = mutableListOf<TrailerYardRecord>()
    for (item in items) {
        val derived = newTrailerYardRecord(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planTrailerYardRecord(item: TrailerYardRecord): String = classifySortationAdjustment(item.measuredValue())
