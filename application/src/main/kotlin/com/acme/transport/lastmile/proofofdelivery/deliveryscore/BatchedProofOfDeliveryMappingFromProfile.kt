package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import com.acme.transport.lastmile.locker.profile.LockerProfile
import com.acme.transport.lastmile.locker.profile.classifyDefaultLockerPlan

fun summarizeLockerProfilesForMapping(items: MutableList<LockerProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveBatchedProofOfDeliveryMappingsFromProfile(items: MutableList<LockerProfile>): MutableList<BatchedProofOfDeliveryMapping> {
    val out = mutableListOf<BatchedProofOfDeliveryMapping>()
    for (item in items) {
        val derived = newBatchedProofOfDeliveryMapping(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyBatchedProofOfDeliveryMapping(item: BatchedProofOfDeliveryMapping): String {
    return classifyDefaultLockerPlan(item.effectiveLoad())
}
