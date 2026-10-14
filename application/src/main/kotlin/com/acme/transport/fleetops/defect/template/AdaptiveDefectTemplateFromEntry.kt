package com.acme.transport.fleetops.defect.template

import com.acme.transport.fleetops.tire.request.TieredTireEntry
import com.acme.transport.fleetops.tire.request.defaultTireManifestBucket

fun summarizeTieredTireEntriesForTemplate(items: MutableList<TieredTireEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveAdaptiveDefectTemplatesFromEntry(items: MutableList<TieredTireEntry>): MutableList<AdaptiveDefectTemplate> {
    val out = mutableListOf<AdaptiveDefectTemplate>()
    for (item in items) {
        val derived = newAdaptiveDefectTemplate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchAdaptiveDefectTemplate(item: AdaptiveDefectTemplate): String {
    return defaultTireManifestBucket(item.projectedExposure())
}
