package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.safety.cameraevent.eventslot.AdaptiveCameraEventRollup
import com.acme.transport.safety.cameraevent.eventslot.fallbackCameraEventSnapshotBucket

fun summarizeAdaptiveCameraEventRollupsForAdjustment(items: MutableList<AdaptiveCameraEventRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveScorecardAdjustmentsFromRollup(items: MutableList<AdaptiveCameraEventRollup>): MutableList<ScorecardAdjustment> {
    val out = mutableListOf<ScorecardAdjustment>()
    for (item in items) {
        val derived = newScorecardAdjustment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchScorecardAdjustment(item: ScorecardAdjustment): String {
    return fallbackCameraEventSnapshotBucket(item.measuredValue())
}
