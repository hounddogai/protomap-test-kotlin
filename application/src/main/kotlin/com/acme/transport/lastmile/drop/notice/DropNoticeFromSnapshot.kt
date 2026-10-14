package com.acme.transport.lastmile.drop.notice

import com.acme.transport.lastmile.locker.snapshot.LockerSnapshot
import com.acme.transport.lastmile.locker.snapshot.lockerAllocationBucket

fun summarizeLockerSnapshotsForNotice(items: MutableList<LockerSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveDropNoticesFromSnapshot(items: MutableList<LockerSnapshot>): MutableList<DropNotice> {
    val out = mutableListOf<DropNotice>()
    for (item in items) {
        val derived = newDropNotice(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchDropNotice(item: DropNotice): String = lockerAllocationBucket(item.weightedVolume())
