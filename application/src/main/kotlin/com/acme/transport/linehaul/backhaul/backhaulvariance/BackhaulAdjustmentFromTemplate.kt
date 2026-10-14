package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.linehaul.trailer.draft.TrailerTemplate

fun summarizeTrailerTemplatesForAdjustment(items: MutableList<TrailerTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveBackhaulAdjustmentsFromTemplate(items: MutableList<TrailerTemplate>): MutableList<BackhaulAdjustment> {
    val out = mutableListOf<BackhaulAdjustment>()
    for (item in items) {
        val derived = newBackhaulAdjustment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}
