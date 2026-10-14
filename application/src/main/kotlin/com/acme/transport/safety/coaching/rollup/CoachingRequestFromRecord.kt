package com.acme.transport.safety.coaching.rollup

import com.acme.transport.safety.nearmiss.rollingmissrecord.GlobalNearMissRecord
import com.acme.transport.safety.nearmiss.rollingmissrecord.pendingNearMissThresholdWeight

fun summarizeGlobalNearMissRecordsForRequest(items: MutableList<GlobalNearMissRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCoachingRequestsFromRecord(items: MutableList<GlobalNearMissRecord>): MutableList<CoachingRequest> {
    val out = mutableListOf<CoachingRequest>()
    for (item in items) {
        val derived = newCoachingRequest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planCoachingRequest(item: CoachingRequest): Double = pendingNearMissThresholdWeight(2, true)
