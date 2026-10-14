package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.network.cutoff.target.CutoffSnapshot
import com.acme.transport.network.cutoff.target.cutoffSnapshotBucket

fun summarizeCutoffSnapshotsForTarget(items: MutableList<CutoffSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveDefaultCutoffTargetsFromSnapshot(items: MutableList<CutoffSnapshot>): MutableList<DefaultCutoffTarget> {
    val out = mutableListOf<DefaultCutoffTarget>()
    for (item in items) {
        val derived = newDefaultCutoffTarget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDefaultCutoffTarget(item: DefaultCutoffTarget): String = cutoffSnapshotBucket(item.adjustedLoad())
