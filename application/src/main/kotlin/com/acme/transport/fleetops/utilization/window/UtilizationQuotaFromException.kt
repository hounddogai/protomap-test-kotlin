package com.acme.transport.fleetops.utilization.window

import com.acme.transport.fleetops.inspection.schedule.InspectionException
import com.acme.transport.fleetops.inspection.schedule.normalizeInspectionCheckpointCode

fun summarizeInspectionExceptionsForQuota(items: MutableList<InspectionException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveUtilizationQuotasFromException(items: MutableList<InspectionException>): MutableList<UtilizationQuota> {
    val out = mutableListOf<UtilizationQuota>()
    for (item in items) {
        val derived = newUtilizationQuota(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankUtilizationQuota(item: UtilizationQuota): String = normalizeInspectionCheckpointCode(item.code)
