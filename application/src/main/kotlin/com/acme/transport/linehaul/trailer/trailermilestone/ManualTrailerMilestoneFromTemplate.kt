package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.linehaul.tender.variance.WeightedTenderTemplate
import com.acme.transport.linehaul.tender.variance.manualTenderVarianceWeight

fun summarizeWeightedTenderTemplatesForMilestone(items: MutableList<WeightedTenderTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveManualTrailerMilestonesFromTemplate(items: MutableList<WeightedTenderTemplate>): MutableList<ManualTrailerMilestone> {
    val out = mutableListOf<ManualTrailerMilestone>()
    for (item in items) {
        val derived = newManualTrailerMilestone(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planManualTrailerMilestone(item: ManualTrailerMilestone): Double = manualTenderVarianceWeight(5, true)
