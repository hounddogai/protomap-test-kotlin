package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.lastmile.locker.schedule.LockerBatch
import com.acme.transport.lastmile.locker.schedule.lockerOverrideBucket

fun summarizeLockerBatchesForSnapshot(items: MutableList<LockerBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveLockerSnapshotsFromBatch(items: MutableList<LockerBatch>): MutableList<LockerSnapshot> {
    val out = mutableListOf<LockerSnapshot>()
    for (item in items) {
        val derived = newLockerSnapshot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planLockerSnapshot(item: LockerSnapshot): String = lockerOverrideBucket(item.weightedLoad())
