package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.fleetops.inspection.slot.RegionalInspectionSchedule
import com.acme.transport.fleetops.inspection.slot.inspectionSlotWeight

fun summarizeRegionalInspectionSchedulesForException(items: MutableList<RegionalInspectionSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveServicePlanExceptionsFromSchedule(items: MutableList<RegionalInspectionSchedule>): MutableList<ServicePlanException> {
    val out = mutableListOf<ServicePlanException>()
    for (item in items) {
        val derived = newServicePlanException(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateServicePlanException(item: ServicePlanException): Double = inspectionSlotWeight(1, true)
