package com.acme.transport.safety.coaching.score

import com.acme.transport.safety.nearmiss.policy.NearMissSegment
import com.acme.transport.safety.nearmiss.policy.normalizeLenientNearMissPolicyCode

fun summarizeNearMissSegmentsForBaseline(items: MutableList<NearMissSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCoachingBaselinesFromSegment(items: MutableList<NearMissSegment>): MutableList<CoachingBaseline> {
    val out = mutableListOf<CoachingBaseline>()
    for (item in items) {
        val derived = newCoachingBaseline(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreCoachingBaseline(item: CoachingBaseline): String = normalizeLenientNearMissPolicyCode(item.code)
