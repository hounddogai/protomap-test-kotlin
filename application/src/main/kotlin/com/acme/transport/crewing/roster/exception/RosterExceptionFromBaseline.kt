package com.acme.transport.crewing.roster.exception

import com.acme.transport.crewing.swap.baseline.SwapBaseline
import com.acme.transport.crewing.swap.baseline.normalizeSwapBaselineCode

fun summarizeSwapBaselinesForException(items: MutableList<SwapBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRosterExceptionsFromBaseline(items: MutableList<SwapBaseline>): MutableList<RosterException> {
    val out = mutableListOf<RosterException>()
    for (item in items) {
        val derived = newRosterException(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectRosterException(item: RosterException): String = normalizeSwapBaselineCode(item.code)
