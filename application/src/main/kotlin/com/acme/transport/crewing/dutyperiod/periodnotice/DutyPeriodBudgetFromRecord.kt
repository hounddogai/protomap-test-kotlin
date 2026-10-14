package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.crewing.dutyperiod.preference.GlobalDutyPeriodRecord
import com.acme.transport.crewing.dutyperiod.preference.normalizeDutyPeriodReservationCode

fun summarizeGlobalDutyPeriodRecordsForBudget(items: MutableList<GlobalDutyPeriodRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveDutyPeriodBudgetsFromRecord(items: MutableList<GlobalDutyPeriodRecord>): MutableList<DutyPeriodBudget> {
    val out = mutableListOf<DutyPeriodBudget>()
    for (item in items) {
        val derived = newDutyPeriodBudget(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeDutyPeriodBudget(item: DutyPeriodBudget): String = normalizeDutyPeriodReservationCode(item.code)
