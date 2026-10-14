package com.acme.transport.lastmile.drop.bundle

import com.acme.transport.lastmile.locker.profile.LockerProfile
import com.acme.transport.lastmile.locker.profile.classifyDefaultLockerPlan

fun summarizeLockerProfilesForBundle(items: MutableList<LockerProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveBatchedDropBundlesFromProfile(items: MutableList<LockerProfile>): MutableList<BatchedDropBundle> {
    val out = mutableListOf<BatchedDropBundle>()
    for (item in items) {
        val derived = newBatchedDropBundle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun scoreBatchedDropBundle(item: BatchedDropBundle): String = classifyDefaultLockerPlan(item.weightedExposure())
