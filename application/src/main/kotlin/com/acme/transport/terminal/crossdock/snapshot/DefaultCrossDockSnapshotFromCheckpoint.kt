package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.terminal.dwell.digest.StrictDwellCheckpoint
import com.acme.transport.terminal.dwell.digest.defaultDwellExceptionWeight

fun summarizeStrictDwellCheckpointsForSnapshot(items: MutableList<StrictDwellCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveDefaultCrossDockSnapshotsFromCheckpoint(items: MutableList<StrictDwellCheckpoint>): MutableList<DefaultCrossDockSnapshot> {
    val out = mutableListOf<DefaultCrossDockSnapshot>()
    for (item in items) {
        val derived = newDefaultCrossDockSnapshot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyDefaultCrossDockSnapshot(item: DefaultCrossDockSnapshot): Double = defaultDwellExceptionWeight(3, true)
