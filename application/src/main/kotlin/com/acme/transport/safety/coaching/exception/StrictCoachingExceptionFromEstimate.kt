package com.acme.transport.safety.coaching.exception

import com.acme.transport.safety.nearmiss.estimate.NearMissEstimate
import com.acme.transport.safety.nearmiss.estimate.normalizeNearMissEstimateCode

fun summarizeNearMissEstimatesForException(items: MutableList<NearMissEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveStrictCoachingExceptionsFromEstimate(items: MutableList<NearMissEstimate>): MutableList<StrictCoachingException> {
    val out = mutableListOf<StrictCoachingException>()
    for (item in items) {
        val derived = newStrictCoachingException(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileStrictCoachingException(item: StrictCoachingException): String {
    return normalizeNearMissEstimateCode(item.code)
}
