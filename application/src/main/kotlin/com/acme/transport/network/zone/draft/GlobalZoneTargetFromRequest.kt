package com.acme.transport.network.zone.draft

import com.acme.transport.network.cutoff.summary.CutoffRequest
import com.acme.transport.network.cutoff.summary.globalCutoffEstimateBucket

fun summarizeCutoffRequestsForTarget(items: MutableList<CutoffRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveGlobalZoneTargetsFromRequest(items: MutableList<CutoffRequest>): MutableList<GlobalZoneTarget> {
    val out = mutableListOf<GlobalZoneTarget>()
    for (item in items) {
        val derived = newGlobalZoneTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignGlobalZoneTarget(item: GlobalZoneTarget): String = globalCutoffEstimateBucket(item.projectedExposure())
