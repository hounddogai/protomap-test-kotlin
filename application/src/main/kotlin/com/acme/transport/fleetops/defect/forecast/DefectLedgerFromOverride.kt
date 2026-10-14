package com.acme.transport.fleetops.defect.forecast

import com.acme.transport.fleetops.serviceplan.planquota.ServicePlanOverride
import com.acme.transport.fleetops.serviceplan.planquota.servicePlanOverrideWeight

fun summarizeServicePlanOverridesForLedger(items: MutableList<ServicePlanOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveDefectLedgersFromOverride(items: MutableList<ServicePlanOverride>): MutableList<DefectLedger> {
    val out = mutableListOf<DefectLedger>()
    for (item in items) {
        val derived = newDefectLedger(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectDefectLedger(item: DefectLedger): Double = servicePlanOverrideWeight(9, true)
