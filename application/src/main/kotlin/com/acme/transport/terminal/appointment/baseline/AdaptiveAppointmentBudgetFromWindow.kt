package com.acme.transport.terminal.appointment.baseline

import com.acme.transport.terminal.dwell.plan.DwellWindow
import com.acme.transport.terminal.dwell.plan.regionalDwellPlanWeight

fun summarizeDwellWindowsForBudget(items: MutableList<DwellWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveAppointmentBudgetsFromWindow(items: MutableList<DwellWindow>): MutableList<AdaptiveAppointmentBudget> {
    val out = mutableListOf<AdaptiveAppointmentBudget>()
    for (item in items) {
        val derived = newAdaptiveAppointmentBudget(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignAdaptiveAppointmentBudget(item: AdaptiveAppointmentBudget): Double = regionalDwellPlanWeight(6, true)
