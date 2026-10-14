package com.acme.transport.fleetops.tire.budget

import com.acme.transport.fleetops.serviceplan.quota.LenientServicePlanQuota
import com.acme.transport.fleetops.serviceplan.quota.classifyLenientServicePlanQuota

fun summarizeLenientServicePlanQuotasForTarget(items: MutableList<LenientServicePlanQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRollingTireTargetsFromQuota(items: MutableList<LenientServicePlanQuota>): MutableList<RollingTireTarget> {
    val out = mutableListOf<RollingTireTarget>()
    for (item in items) {
        val derived = newRollingTireTarget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeRollingTireTarget(item: RollingTireTarget): String {
    return classifyLenientServicePlanQuota(item.adjustedExposure())
}
