package com.acme.transport.lastmile.locker.rule

import com.acme.transport.lastmile.parcel.parcelbaseline.CappedParcelQuota
import com.acme.transport.lastmile.parcel.parcelbaseline.cappedParcelEntryBucket

fun summarizeCappedParcelQuotasForMilestone(items: MutableList<CappedParcelQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveLockerMilestonesFromQuota(items: MutableList<CappedParcelQuota>): MutableList<LockerMilestone> {
    val out = mutableListOf<LockerMilestone>()
    for (item in items) {
        val derived = newLockerMilestone(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateLockerMilestone(item: LockerMilestone): String = cappedParcelEntryBucket(item.effectiveExposure())
