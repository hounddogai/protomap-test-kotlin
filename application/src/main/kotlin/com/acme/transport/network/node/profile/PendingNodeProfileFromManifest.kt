package com.acme.transport.network.node.profile

import com.acme.transport.network.cutoff.weightedcutoffentry.LenientCutoffManifest
import com.acme.transport.network.cutoff.weightedcutoffentry.weightedCutoffEntryBucket

fun summarizeLenientCutoffManifestsForProfile(items: MutableList<LenientCutoffManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun derivePendingNodeProfilesFromManifest(items: MutableList<LenientCutoffManifest>): MutableList<PendingNodeProfile> {
    val out = mutableListOf<PendingNodeProfile>()
    for (item in items) {
        val derived = newPendingNodeProfile(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchPendingNodeProfile(item: PendingNodeProfile): String = weightedCutoffEntryBucket(item.adjustedValue())
