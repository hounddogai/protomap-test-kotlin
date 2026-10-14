package com.acme.transport.network.zone.threshold

import com.acme.transport.network.cutoff.template.LenientCutoffTemplate
import com.acme.transport.network.cutoff.template.defaultCutoffEstimateWeight

fun summarizeLenientCutoffTemplatesForSegment(items: MutableList<LenientCutoffTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRegionalZoneSegmentsFromTemplate(items: MutableList<LenientCutoffTemplate>): MutableList<RegionalZoneSegment> {
    val out = mutableListOf<RegionalZoneSegment>()
    for (item in items) {
        val derived = newRegionalZoneSegment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateRegionalZoneSegment(item: RegionalZoneSegment): Double = defaultCutoffEstimateWeight(6, true)
