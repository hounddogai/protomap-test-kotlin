package com.acme.transport.safety.coaching.coachingscore

import com.acme.transport.safety.incident.metric.DefaultIncidentCycle
import com.acme.transport.safety.incident.metric.normalizeDefaultIncidentCycleCode

fun summarizeDefaultIncidentCyclesForScore(items: MutableList<DefaultIncidentCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveRollingCoachingScoresFromCycle(items: MutableList<DefaultIncidentCycle>): MutableList<RollingCoachingScore> {
    val out = mutableListOf<RollingCoachingScore>()
    for (item in items) {
        val derived = newRollingCoachingScore(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignRollingCoachingScore(item: RollingCoachingScore): String = normalizeDefaultIncidentCycleCode(item.code)
