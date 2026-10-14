package com.acme.transport.network.hub.batchedhubmilestone

import com.acme.transport.network.zone.entry.GlobalZoneClaim
import com.acme.transport.network.zone.entry.globalZoneClaimBucket

fun summarizeGlobalZoneClaimsForThreshold(items: MutableList<GlobalZoneClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveLenientHubThresholdsFromClaim(items: MutableList<GlobalZoneClaim>): MutableList<LenientHubThreshold> {
    val out = mutableListOf<LenientHubThreshold>()
    for (item in items) {
        val derived = newLenientHubThreshold(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectLenientHubThreshold(item: LenientHubThreshold): String = globalZoneClaimBucket(item.measuredValue())
