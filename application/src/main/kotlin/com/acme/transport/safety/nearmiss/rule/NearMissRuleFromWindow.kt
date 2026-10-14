package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.safety.incident.group.IncidentWindow
import com.acme.transport.safety.incident.group.incidentWindowWeight

fun summarizeIncidentWindowsForRule(items: MutableList<IncidentWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveNearMissRulesFromWindow(items: MutableList<IncidentWindow>): MutableList<NearMissRule> {
    val out = mutableListOf<NearMissRule>()
    for (item in items) {
        val derived = newNearMissRule(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeNearMissRule(item: NearMissRule): Double = incidentWindowWeight(4, true)
