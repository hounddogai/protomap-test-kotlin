package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.lastmile.locker.variance.StrictLockerTarget
import com.acme.transport.lastmile.locker.variance.classifyStrictLockerTarget

fun summarizeStrictLockerTargetsForSchedule(items: MutableList<StrictLockerTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveAdaptiveProofOfDeliverySchedulesFromTarget(items: MutableList<StrictLockerTarget>): MutableList<AdaptiveProofOfDeliverySchedule> {
    val out = mutableListOf<AdaptiveProofOfDeliverySchedule>()
    for (item in items) {
        val derived = newAdaptiveProofOfDeliverySchedule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitAdaptiveProofOfDeliverySchedule(item: AdaptiveProofOfDeliverySchedule): String {
    return classifyStrictLockerTarget(item.weightedExposure())
}
