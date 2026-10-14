package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.fleetops.tire.tirewindow.DefaultTireQuota
import com.acme.transport.fleetops.tire.tirewindow.normalizeRollingTireCycleCode

fun summarizeDefaultTireQuotasForCycle(items: MutableList<DefaultTireQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveServicePlanCyclesFromQuota(items: MutableList<DefaultTireQuota>): MutableList<ServicePlanCycle> {
    val out = mutableListOf<ServicePlanCycle>()
    for (item in items) {
        val derived = newServicePlanCycle(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeServicePlanCycle(item: ServicePlanCycle): String = normalizeRollingTireCycleCode(item.code)
