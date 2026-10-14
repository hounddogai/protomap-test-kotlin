package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.terminal.sortation.template.SortationTemplate
import com.acme.transport.terminal.sortation.template.normalizeSortationMetricCode

fun summarizeSortationTemplatesForGroup(items: MutableList<SortationTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveDwellGroupsFromTemplate(items: MutableList<SortationTemplate>): MutableList<DwellGroup> {
    val out = mutableListOf<DwellGroup>()
    for (item in items) {
        val derived = newDwellGroup(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankDwellGroup(item: DwellGroup): String = normalizeSortationMetricCode(item.code)
