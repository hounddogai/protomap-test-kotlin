package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.fleetops.inspection.score.PrimaryInspectionException
import com.acme.transport.fleetops.inspection.score.classifyPrimaryInspectionException

fun summarizePrimaryInspectionExceptionsForSummary(items: MutableList<PrimaryInspectionException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingTireSummariesFromException(items: MutableList<PrimaryInspectionException>): MutableList<RollingTireSummary> {
    val out = mutableListOf<RollingTireSummary>()
    for (item in items) {
        val derived = newRollingTireSummary(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreRollingTireSummary(item: RollingTireSummary): String {
    return classifyPrimaryInspectionException(item.weightedTotal())
}
