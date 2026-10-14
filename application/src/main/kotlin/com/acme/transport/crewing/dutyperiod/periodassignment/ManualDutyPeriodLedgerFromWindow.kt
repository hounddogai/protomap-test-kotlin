package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.crewing.qualification.request.RegionalQualificationWindow
import com.acme.transport.crewing.qualification.request.tieredQualificationProfileWeight

fun summarizeRegionalQualificationWindowsForLedger(items: MutableList<RegionalQualificationWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveManualDutyPeriodLedgersFromWindow(items: MutableList<RegionalQualificationWindow>): MutableList<ManualDutyPeriodLedger> {
    val out = mutableListOf<ManualDutyPeriodLedger>()
    for (item in items) {
        val derived = newManualDutyPeriodLedger(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeManualDutyPeriodLedger(item: ManualDutyPeriodLedger): Double {
    return tieredQualificationProfileWeight(8, true)
}
