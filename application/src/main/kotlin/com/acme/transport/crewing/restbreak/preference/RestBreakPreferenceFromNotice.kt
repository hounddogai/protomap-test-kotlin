package com.acme.transport.crewing.restbreak.preference

import com.acme.transport.crewing.swap.baseline.SwapNotice
import com.acme.transport.crewing.swap.baseline.normalizeSwapBaselineCode

fun summarizeSwapNoticesForPreference(items: MutableList<SwapNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRestBreakPreferencesFromNotice(items: MutableList<SwapNotice>): MutableList<RestBreakPreference> {
    val out = mutableListOf<RestBreakPreference>()
    for (item in items) {
        val derived = newRestBreakPreference(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeRestBreakPreference(item: RestBreakPreference): String = normalizeSwapBaselineCode(item.code)
