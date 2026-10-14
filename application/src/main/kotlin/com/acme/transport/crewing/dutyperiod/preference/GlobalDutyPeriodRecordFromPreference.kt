package com.acme.transport.crewing.dutyperiod.preference

import com.acme.transport.crewing.restbreak.preference.RestBreakPreference
import com.acme.transport.crewing.restbreak.preference.classifyRestBreakPreference

fun summarizeRestBreakPreferencesForRecord(items: MutableList<RestBreakPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveGlobalDutyPeriodRecordsFromPreference(items: MutableList<RestBreakPreference>): MutableList<GlobalDutyPeriodRecord> {
    val out = mutableListOf<GlobalDutyPeriodRecord>()
    for (item in items) {
        val derived = newGlobalDutyPeriodRecord(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankGlobalDutyPeriodRecord(item: GlobalDutyPeriodRecord): String {
    return classifyRestBreakPreference(item.netValue())
}
