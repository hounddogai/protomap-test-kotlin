package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.linehaul.laneplan.planwindow.StrictLanePlanWindow
import com.acme.transport.linehaul.laneplan.planwindow.regionalLanePlanBudgetWeight

fun summarizeStrictLanePlanWindowsForTemplate(items: MutableList<StrictLanePlanWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveTieredLanePlanTemplatesFromWindow(items: MutableList<StrictLanePlanWindow>): MutableList<TieredLanePlanTemplate> {
    val out = mutableListOf<TieredLanePlanTemplate>()
    for (item in items) {
        val derived = newTieredLanePlanTemplate(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignTieredLanePlanTemplate(item: TieredLanePlanTemplate): Double = regionalLanePlanBudgetWeight(1, true)
