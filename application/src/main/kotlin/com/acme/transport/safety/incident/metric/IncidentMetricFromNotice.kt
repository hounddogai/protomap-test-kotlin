package com.acme.transport.safety.incident.metric

import com.acme.transport.safety.coaching.assignment.ManualCoachingNotice
import com.acme.transport.safety.coaching.assignment.coachingAssignmentBucket

fun summarizeManualCoachingNoticesForMetric(items: MutableList<ManualCoachingNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveIncidentMetricsFromNotice(items: MutableList<ManualCoachingNotice>): MutableList<IncidentMetric> {
    val out = mutableListOf<IncidentMetric>()
    for (item in items) {
        val derived = newIncidentMetric(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeIncidentMetric(item: IncidentMetric): String = coachingAssignmentBucket(item.measuredValue())
