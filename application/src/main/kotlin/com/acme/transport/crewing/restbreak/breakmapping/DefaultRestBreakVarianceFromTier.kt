package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.crewing.dutyperiod.perioddraft.DutyPeriodTier
import com.acme.transport.crewing.dutyperiod.perioddraft.normalizePendingDutyPeriodBudgetCode

fun summarizeDutyPeriodTiersForVariance(items: MutableList<DutyPeriodTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultRestBreakVariancesFromTier(items: MutableList<DutyPeriodTier>): MutableList<DefaultRestBreakVariance> {
    val out = mutableListOf<DefaultRestBreakVariance>()
    for (item in items) {
        val derived = newDefaultRestBreakVariance(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeDefaultRestBreakVariance(item: DefaultRestBreakVariance): String {
    return normalizePendingDutyPeriodBudgetCode(item.code)
}
