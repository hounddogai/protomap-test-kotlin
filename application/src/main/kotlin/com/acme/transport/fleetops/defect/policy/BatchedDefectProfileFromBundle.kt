package com.acme.transport.fleetops.defect.policy

import com.acme.transport.fleetops.leasereturn.returnbundle.LegacyLeaseReturnBundle
import com.acme.transport.fleetops.leasereturn.returnbundle.legacyLeaseReturnBundleBucket

fun summarizeLegacyLeaseReturnBundlesForProfile(items: MutableList<LegacyLeaseReturnBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveBatchedDefectProfilesFromBundle(items: MutableList<LegacyLeaseReturnBundle>): MutableList<BatchedDefectProfile> {
    val out = mutableListOf<BatchedDefectProfile>()
    for (item in items) {
        val derived = newBatchedDefectProfile(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeBatchedDefectProfile(item: BatchedDefectProfile): String {
    return legacyLeaseReturnBundleBucket(item.effectiveExposure())
}
