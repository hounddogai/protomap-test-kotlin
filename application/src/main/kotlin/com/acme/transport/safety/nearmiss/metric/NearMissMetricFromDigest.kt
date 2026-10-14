package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.safety.scorecard.schedule.PrimaryScorecardDigest
import com.acme.transport.safety.scorecard.schedule.normalizeScorecardScheduleCode

fun summarizePrimaryScorecardDigestsForMetric(items: MutableList<PrimaryScorecardDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveNearMissMetricsFromDigest(items: MutableList<PrimaryScorecardDigest>): MutableList<NearMissMetric> {
    val out = mutableListOf<NearMissMetric>()
    for (item in items) {
        val derived = newNearMissMetric(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocateNearMissMetric(item: NearMissMetric): String = normalizeScorecardScheduleCode(item.code)
