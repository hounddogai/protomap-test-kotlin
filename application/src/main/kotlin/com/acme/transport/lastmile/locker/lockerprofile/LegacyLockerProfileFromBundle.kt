package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.lastmile.drop.bundle.BatchedDropBundle
import com.acme.transport.lastmile.drop.bundle.batchedDropBundleWeight

fun summarizeBatchedDropBundlesForProfile(items: MutableList<BatchedDropBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveLegacyLockerProfilesFromBundle(items: MutableList<BatchedDropBundle>): MutableList<LegacyLockerProfile> {
    val out = mutableListOf<LegacyLockerProfile>()
    for (item in items) {
        val derived = newLegacyLockerProfile(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectLegacyLockerProfile(item: LegacyLockerProfile): Double = batchedDropBundleWeight(3, true)
