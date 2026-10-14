package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.linehaul.laneplan.budget.LanePlanBudget
import com.acme.transport.linehaul.laneplan.budget.normalizeLenientLanePlanPreferenceCode

fun summarizeLanePlanBudgetsForReservation(items: MutableList<LanePlanBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveScheduledLanePlanReservationsFromBudget(items: MutableList<LanePlanBudget>): MutableList<ScheduledLanePlanReservation> {
    val out = mutableListOf<ScheduledLanePlanReservation>()
    for (item in items) {
        val derived = newScheduledLanePlanReservation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyScheduledLanePlanReservation(item: ScheduledLanePlanReservation): String {
    return normalizeLenientLanePlanPreferenceCode(item.code)
}
