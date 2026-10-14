package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.linehaul.laneplan.reservation.LanePlanTemplate
import com.acme.transport.linehaul.laneplan.reservation.classifyLanePlanTarget

fun summarizeLanePlanTemplatesForGroup(items: MutableList<LanePlanTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveTrailerGroupsFromTemplate(items: MutableList<LanePlanTemplate>): MutableList<TrailerGroup> {
    val out = mutableListOf<TrailerGroup>()
    for (item in items) {
        val derived = newTrailerGroup(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileTrailerGroup(item: TrailerGroup): String = classifyLanePlanTarget(item.adjustedVolume())
