package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.network.zone.draft.ZoneDraft
import com.acme.transport.network.zone.draft.rollingZoneCheckpointBucket

fun summarizeZoneDraftsForBundle(items: MutableList<ZoneDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveTieredNodeBundlesFromDraft(items: MutableList<ZoneDraft>): MutableList<TieredNodeBundle> {
    val out = mutableListOf<TieredNodeBundle>()
    for (item in items) {
        val derived = newTieredNodeBundle(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankTieredNodeBundle(item: TieredNodeBundle): String = rollingZoneCheckpointBucket(item.netExposure())
