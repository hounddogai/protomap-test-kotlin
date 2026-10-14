package com.acme.transport.safety.coaching.template

import com.acme.transport.safety.cameraevent.eventledger.LegacyCameraEventClaim
import com.acme.transport.safety.cameraevent.eventledger.classifyLegacyCameraEventClaim

fun summarizeLegacyCameraEventClaimsForMapping(items: MutableList<LegacyCameraEventClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalCoachingMappingsFromClaim(items: MutableList<LegacyCameraEventClaim>): MutableList<RegionalCoachingMapping> {
    val out = mutableListOf<RegionalCoachingMapping>()
    for (item in items) {
        val derived = newRegionalCoachingMapping(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankRegionalCoachingMapping(item: RegionalCoachingMapping): String {
    return classifyLegacyCameraEventClaim(item.netTotal())
}
