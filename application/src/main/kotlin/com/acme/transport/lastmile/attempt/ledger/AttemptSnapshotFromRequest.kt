package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.lastmile.parcel.score.PendingParcelRequest
import com.acme.transport.lastmile.parcel.score.classifyLegacyParcelScore

fun summarizePendingParcelRequestsForSnapshot(items: MutableList<PendingParcelRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveAttemptSnapshotsFromRequest(items: MutableList<PendingParcelRequest>): MutableList<AttemptSnapshot> {
    val out = mutableListOf<AttemptSnapshot>()
    for (item in items) {
        val derived = newAttemptSnapshot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeAttemptSnapshot(item: AttemptSnapshot): String = classifyLegacyParcelScore(item.adjustedTotal())
