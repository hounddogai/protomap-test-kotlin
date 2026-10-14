package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.lastmile.drop.dropallocation.GlobalDropAllocation
import com.acme.transport.lastmile.drop.dropallocation.normalizeDropClaimCode

fun summarizeGlobalDropAllocationsForManifest(items: MutableList<GlobalDropAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveDefaultParcelManifestsFromAllocation(items: MutableList<GlobalDropAllocation>): MutableList<DefaultParcelManifest> {
    val out = mutableListOf<DefaultParcelManifest>()
    for (item in items) {
        val derived = newDefaultParcelManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDefaultParcelManifest(item: DefaultParcelManifest): String = normalizeDropClaimCode(item.code)
