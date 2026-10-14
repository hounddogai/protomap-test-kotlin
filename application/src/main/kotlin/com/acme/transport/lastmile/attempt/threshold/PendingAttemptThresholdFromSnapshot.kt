package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.lastmile.drop.snapshot.LenientDropSnapshot
import com.acme.transport.lastmile.drop.snapshot.lenientDropSnapshotWeight

fun summarizeLenientDropSnapshotsForThreshold(items: MutableList<LenientDropSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun derivePendingAttemptThresholdsFromSnapshot(items: MutableList<LenientDropSnapshot>): MutableList<PendingAttemptThreshold> {
    val out = mutableListOf<PendingAttemptThreshold>()
    for (item in items) {
        val derived = newPendingAttemptThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergePendingAttemptThreshold(item: PendingAttemptThreshold): Double = lenientDropSnapshotWeight(6, true)
