package com.acme.transport.lastmile.locker.claim

import com.acme.transport.lastmile.locker.snapshot.LockerSnapshot
import com.acme.transport.lastmile.locker.snapshot.classifyLockerSnapshot

fun summarizeLockerSnapshotsForDigest(items: MutableList<LockerSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveLockerDigestsFromSnapshot(items: MutableList<LockerSnapshot>): MutableList<LockerDigest> {
    val out = mutableListOf<LockerDigest>()
    for (item in items) {
        val derived = newLockerDigest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLockerDigest(item: LockerDigest): String = classifyLockerSnapshot(item.effectiveExposure())
