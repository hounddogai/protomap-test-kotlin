package com.acme.transport.terminal.dwell.digest

import com.acme.transport.terminal.dwell.plan.RegionalDwellPlan
import com.acme.transport.terminal.dwell.plan.classifyRegionalDwellPlan

fun summarizeRegionalDwellPlansForException(items: MutableList<RegionalDwellPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDefaultDwellExceptionsFromPlan(items: MutableList<RegionalDwellPlan>): MutableList<DefaultDwellException> {
    val out = mutableListOf<DefaultDwellException>()
    for (item in items) {
        val derived = newDefaultDwellException(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeDefaultDwellException(item: DefaultDwellException): String {
    return classifyRegionalDwellPlan(item.measuredValue())
}
