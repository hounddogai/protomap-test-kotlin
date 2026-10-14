package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.safety.nearmiss.misscycle.NearMissDraft
import com.acme.transport.safety.nearmiss.misscycle.defaultNearMissOverrideBucket

fun summarizeNearMissDraftsForScore(items: MutableList<NearMissDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveDefaultCameraEventScoresFromDraft(items: MutableList<NearMissDraft>): MutableList<DefaultCameraEventScore> {
    val out = mutableListOf<DefaultCameraEventScore>()
    for (item in items) {
        val derived = newDefaultCameraEventScore(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitDefaultCameraEventScore(item: DefaultCameraEventScore): String {
    return defaultNearMissOverrideBucket(item.effectiveExposure())
}
