package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.crewing.restbreak.preference.RestBreakPreference
import com.acme.transport.crewing.restbreak.preference.normalizeRestBreakPreferenceCode

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

fun deriveRestBreakRecordsFromPreference(items: MutableList<RestBreakPreference>): MutableList<RestBreakRecord> {
    val out = mutableListOf<RestBreakRecord>()
    for (item in items) {
        val derived = newRestBreakRecord(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planRestBreakRecord(item: RestBreakRecord): String = normalizeRestBreakPreferenceCode(item.code)
