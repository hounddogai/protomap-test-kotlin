package com.acme.transport.lastmile.locker.policy

import com.acme.transport.lastmile.proofofdelivery.preference.WeightedProofOfDeliveryPreference

fun summarizeWeightedProofOfDeliveryPreferencesForTarget(items: MutableList<WeightedProofOfDeliveryPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLockerTargetsFromPreference(items: MutableList<WeightedProofOfDeliveryPreference>): MutableList<LockerTarget> {
    val out = mutableListOf<LockerTarget>()
    for (item in items) {
        val derived = newLockerTarget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}
