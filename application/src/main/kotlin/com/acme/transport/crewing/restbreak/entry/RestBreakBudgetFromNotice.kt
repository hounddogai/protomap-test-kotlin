package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.crewing.qualification.metric.ScheduledQualificationNotice
import com.acme.transport.crewing.qualification.metric.classifyScheduledQualificationNotice

fun summarizeScheduledQualificationNoticesForBudget(items: MutableList<ScheduledQualificationNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveRestBreakBudgetsFromNotice(items: MutableList<ScheduledQualificationNotice>): MutableList<RestBreakBudget> {
    val out = mutableListOf<RestBreakBudget>()
    for (item in items) {
        val derived = newRestBreakBudget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeRestBreakBudget(item: RestBreakBudget): String {
    return classifyScheduledQualificationNotice(item.adjustedTotal())
}
