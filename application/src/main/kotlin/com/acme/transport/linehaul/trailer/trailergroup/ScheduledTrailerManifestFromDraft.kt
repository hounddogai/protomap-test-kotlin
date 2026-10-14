package com.acme.transport.linehaul.trailer.trailergroup

import com.acme.transport.linehaul.laneplan.planthreshold.RollingLanePlanDraft
import com.acme.transport.linehaul.laneplan.planthreshold.tieredLanePlanThresholdWeight

fun summarizeRollingLanePlanDraftsForManifest(items: MutableList<RollingLanePlanDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveScheduledTrailerManifestsFromDraft(items: MutableList<RollingLanePlanDraft>): MutableList<ScheduledTrailerManifest> {
    val out = mutableListOf<ScheduledTrailerManifest>()
    for (item in items) {
        val derived = newScheduledTrailerManifest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyScheduledTrailerManifest(item: ScheduledTrailerManifest): Double = tieredLanePlanThresholdWeight(1, true)
