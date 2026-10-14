package com.acme.transport.lastmile.attempt.attemptdigest

import com.acme.transport.lastmile.parcel.tieredparcelvariance.RollingParcelVariance
import com.acme.transport.lastmile.parcel.tieredparcelvariance.classifyManualParcelClaim

fun summarizeRollingParcelVariancesForSlot(items: MutableList<RollingParcelVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveAttemptSlotsFromVariance(items: MutableList<RollingParcelVariance>): MutableList<AttemptSlot> {
    val out = mutableListOf<AttemptSlot>()
    for (item in items) {
        val derived = newAttemptSlot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planAttemptSlot(item: AttemptSlot): String = classifyManualParcelClaim(item.netExposure())
