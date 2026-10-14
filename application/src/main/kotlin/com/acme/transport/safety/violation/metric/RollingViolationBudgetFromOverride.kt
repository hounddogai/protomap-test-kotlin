package com.acme.transport.safety.violation.metric

import com.acme.transport.safety.incident.incidentoverride.TieredIncidentOverride
import com.acme.transport.safety.incident.incidentoverride.normalizeCappedIncidentTierCode

fun summarizeTieredIncidentOverridesForBudget(items: MutableList<TieredIncidentOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRollingViolationBudgetsFromOverride(items: MutableList<TieredIncidentOverride>): MutableList<RollingViolationBudget> {
    val out = mutableListOf<RollingViolationBudget>()
    for (item in items) {
        val derived = newRollingViolationBudget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankRollingViolationBudget(item: RollingViolationBudget): String = normalizeCappedIncidentTierCode(item.code)
