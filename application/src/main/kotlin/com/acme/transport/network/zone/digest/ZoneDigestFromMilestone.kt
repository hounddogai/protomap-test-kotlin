package com.acme.transport.network.zone.digest

import com.acme.transport.network.cutoff.batch.PendingCutoffMilestone
import com.acme.transport.network.cutoff.batch.classifyStrictCutoffRollup

fun summarizePendingCutoffMilestonesForDigest(items: MutableList<PendingCutoffMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveZoneDigestsFromMilestone(items: MutableList<PendingCutoffMilestone>): MutableList<ZoneDigest> {
    val out = mutableListOf<ZoneDigest>()
    for (item in items) {
        val derived = newZoneDigest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeZoneDigest(item: ZoneDigest): String = classifyStrictCutoffRollup(item.projectedExposure())
