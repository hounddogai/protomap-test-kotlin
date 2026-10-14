package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.terminal.dwell.dwellpreference.DwellLedger
import com.acme.transport.terminal.dwell.dwellpreference.dwellForecastWeight

fun summarizeDwellLedgersForBundle(items: MutableList<DwellLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveFallbackDwellBundlesFromLedger(items: MutableList<DwellLedger>): MutableList<FallbackDwellBundle> {
    val out = mutableListOf<FallbackDwellBundle>()
    for (item in items) {
        val derived = newFallbackDwellBundle(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyFallbackDwellBundle(item: FallbackDwellBundle): Double = dwellForecastWeight(2, true)
