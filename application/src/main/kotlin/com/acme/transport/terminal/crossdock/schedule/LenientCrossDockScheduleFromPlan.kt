package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.terminal.traileryard.plan.PrimaryTrailerYardPlan
import com.acme.transport.terminal.traileryard.plan.classifyPrimaryTrailerYardPlan

fun summarizePrimaryTrailerYardPlansForSchedule(items: MutableList<PrimaryTrailerYardPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveLenientCrossDockSchedulesFromPlan(items: MutableList<PrimaryTrailerYardPlan>): MutableList<LenientCrossDockSchedule> {
    val out = mutableListOf<LenientCrossDockSchedule>()
    for (item in items) {
        val derived = newLenientCrossDockSchedule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchLenientCrossDockSchedule(item: LenientCrossDockSchedule): String {
    return classifyPrimaryTrailerYardPlan(item.weightedTotal())
}
