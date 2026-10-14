package com.acme.transport.network.cutoff.entry

import com.acme.transport.network.cutoff.draft.CutoffManifest
import com.acme.transport.network.cutoff.draft.cutoffDraftBucket

fun summarizeCutoffManifestsForAllocation(items: MutableList<CutoffManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveCutoffAllocationsFromManifest(items: MutableList<CutoffManifest>): MutableList<CutoffAllocation> {
    val out = mutableListOf<CutoffAllocation>()
    for (item in items) {
        val derived = newCutoffAllocation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeCutoffAllocation(item: CutoffAllocation): String = cutoffDraftBucket(item.netExposure())
