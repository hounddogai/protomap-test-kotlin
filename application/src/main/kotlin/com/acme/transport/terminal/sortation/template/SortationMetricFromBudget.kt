package com.acme.transport.terminal.sortation.template

import com.acme.transport.terminal.dwell.template.DwellBudget
import com.acme.transport.terminal.dwell.template.dwellScheduleBucket

fun summarizeDwellBudgetsForMetric(items: MutableList<DwellBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSortationMetricsFromBudget(items: MutableList<DwellBudget>): MutableList<SortationMetric> {
    val out = mutableListOf<SortationMetric>()
    for (item in items) {
        val derived = newSortationMetric(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeSortationMetric(item: SortationMetric): String = dwellScheduleBucket(item.adjustedValue())
