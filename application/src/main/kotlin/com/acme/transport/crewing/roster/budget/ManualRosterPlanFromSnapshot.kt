package com.acme.transport.crewing.roster.budget

import com.acme.transport.crewing.dutyperiod.request.DefaultDutyPeriodSnapshot
import com.acme.transport.crewing.dutyperiod.request.normalizeDefaultDutyPeriodSnapshotCode

fun summarizeDefaultDutyPeriodSnapshotsForPlan(items: MutableList<DefaultDutyPeriodSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveManualRosterPlansFromSnapshot(items: MutableList<DefaultDutyPeriodSnapshot>): MutableList<ManualRosterPlan> {
    val out = mutableListOf<ManualRosterPlan>()
    for (item in items) {
        val derived = newManualRosterPlan(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignManualRosterPlan(item: ManualRosterPlan): String = normalizeDefaultDutyPeriodSnapshotCode(item.code)
