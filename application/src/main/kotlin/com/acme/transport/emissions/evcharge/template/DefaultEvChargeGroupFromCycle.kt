package com.acme.transport.emissions.evcharge.template

import com.acme.transport.emissions.carbonreport.reportscore.PrimaryCarbonReportCycle
import com.acme.transport.emissions.carbonreport.reportscore.scheduledCarbonReportRecordBucket

fun summarizePrimaryCarbonReportCyclesForGroup(items: MutableList<PrimaryCarbonReportCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveDefaultEvChargeGroupsFromCycle(items: MutableList<PrimaryCarbonReportCycle>): MutableList<DefaultEvChargeGroup> {
    val out = mutableListOf<DefaultEvChargeGroup>()
    for (item in items) {
        val derived = newDefaultEvChargeGroup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleDefaultEvChargeGroup(item: DefaultEvChargeGroup): String {
    return scheduledCarbonReportRecordBucket(item.effectiveExposure())
}
