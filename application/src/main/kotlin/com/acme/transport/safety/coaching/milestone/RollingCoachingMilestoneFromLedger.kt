package com.acme.transport.safety.coaching.milestone

import com.acme.transport.safety.cameraevent.ledger.WeightedCameraEventLedger
import com.acme.transport.safety.cameraevent.ledger.normalizeCameraEventEstimateCode

fun summarizeWeightedCameraEventLedgersForMilestone(items: MutableList<WeightedCameraEventLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveRollingCoachingMilestonesFromLedger(items: MutableList<WeightedCameraEventLedger>): MutableList<RollingCoachingMilestone> {
    val out = mutableListOf<RollingCoachingMilestone>()
    for (item in items) {
        val derived = newRollingCoachingMilestone(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateRollingCoachingMilestone(item: RollingCoachingMilestone): String {
    return normalizeCameraEventEstimateCode(item.code)
}
