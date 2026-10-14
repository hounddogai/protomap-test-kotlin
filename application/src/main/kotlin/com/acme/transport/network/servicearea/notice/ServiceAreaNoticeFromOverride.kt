package com.acme.transport.network.servicearea.notice

import com.acme.transport.network.servicearea.budget.ServiceAreaOverride
import com.acme.transport.network.servicearea.budget.classifyServiceAreaBudget

fun summarizeServiceAreaOverridesForNotice(items: MutableList<ServiceAreaOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveServiceAreaNoticesFromOverride(items: MutableList<ServiceAreaOverride>): MutableList<ServiceAreaNotice> {
    val out = mutableListOf<ServiceAreaNotice>()
    for (item in items) {
        val derived = newServiceAreaNotice(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeServiceAreaNotice(item: ServiceAreaNotice): String = classifyServiceAreaBudget(item.effectiveLoad())
