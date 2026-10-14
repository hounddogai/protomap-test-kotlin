package com.acme.transport.terminal.dwell.dwellpreference

import com.acme.transport.terminal.crossdock.docknotice.AdaptiveCrossDockAdjustment
import com.acme.transport.terminal.crossdock.docknotice.adaptiveCrossDockNoticeWeight

fun summarizeAdaptiveCrossDockAdjustmentsForPreference(items: MutableList<AdaptiveCrossDockAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveDefaultDwellPreferencesFromAdjustment(items: MutableList<AdaptiveCrossDockAdjustment>): MutableList<DefaultDwellPreference> {
    val out = mutableListOf<DefaultDwellPreference>()
    for (item in items) {
        val derived = newDefaultDwellPreference(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeDefaultDwellPreference(item: DefaultDwellPreference): Double = adaptiveCrossDockNoticeWeight(7, true)
