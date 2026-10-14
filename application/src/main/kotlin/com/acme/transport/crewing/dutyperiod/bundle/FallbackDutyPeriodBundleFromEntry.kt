package com.acme.transport.crewing.dutyperiod.bundle

import com.acme.transport.crewing.dutyperiod.group.DutyPeriodEntry
import com.acme.transport.crewing.dutyperiod.group.normalizeDutyPeriodGroupCode

fun summarizeDutyPeriodEntriesForBundle(items: MutableList<DutyPeriodEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveFallbackDutyPeriodBundlesFromEntry(items: MutableList<DutyPeriodEntry>): MutableList<FallbackDutyPeriodBundle> {
    val out = mutableListOf<FallbackDutyPeriodBundle>()
    for (item in items) {
        val derived = newFallbackDutyPeriodBundle(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planFallbackDutyPeriodBundle(item: FallbackDutyPeriodBundle): String = normalizeDutyPeriodGroupCode(item.code)
