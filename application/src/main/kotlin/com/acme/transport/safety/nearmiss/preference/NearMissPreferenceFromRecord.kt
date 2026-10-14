package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.safety.cameraevent.eventrecord.PrimaryCameraEventRecord
import com.acme.transport.safety.cameraevent.eventrecord.fallbackCameraEventSummaryWeight

fun summarizePrimaryCameraEventRecordsForPreference(items: MutableList<PrimaryCameraEventRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveNearMissPreferencesFromRecord(items: MutableList<PrimaryCameraEventRecord>): MutableList<NearMissPreference> {
    val out = mutableListOf<NearMissPreference>()
    for (item in items) {
        val derived = newNearMissPreference(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeNearMissPreference(item: NearMissPreference): Double = fallbackCameraEventSummaryWeight(9, true)
