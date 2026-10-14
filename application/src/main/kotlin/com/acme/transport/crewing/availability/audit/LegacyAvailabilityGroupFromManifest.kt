package com.acme.transport.crewing.availability.audit

import com.acme.transport.crewing.restbreak.summary.RestBreakManifest
import com.acme.transport.crewing.restbreak.summary.restBreakManifestBucket

fun summarizeRestBreakManifestsForGroup(items: MutableList<RestBreakManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveLegacyAvailabilityGroupsFromManifest(items: MutableList<RestBreakManifest>): MutableList<LegacyAvailabilityGroup> {
    val out = mutableListOf<LegacyAvailabilityGroup>()
    for (item in items) {
        val derived = newLegacyAvailabilityGroup(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleLegacyAvailabilityGroup(item: LegacyAvailabilityGroup): String {
    return restBreakManifestBucket(item.measuredValue())
}
