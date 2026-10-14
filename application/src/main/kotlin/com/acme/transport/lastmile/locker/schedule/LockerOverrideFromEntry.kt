package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.lastmile.parcel.milestone.PrimaryParcelEntry
import com.acme.transport.lastmile.parcel.milestone.parcelMilestoneBucket

fun summarizePrimaryParcelEntriesForOverride(items: MutableList<PrimaryParcelEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveLockerOverridesFromEntry(items: MutableList<PrimaryParcelEntry>): MutableList<LockerOverride> {
    val out = mutableListOf<LockerOverride>()
    for (item in items) {
        val derived = newLockerOverride(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun scoreLockerOverride(item: LockerOverride): String = parcelMilestoneBucket(item.measuredValue())
