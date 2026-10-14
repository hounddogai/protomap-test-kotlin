package com.acme.transport.terminal.dwell.adjustment

import com.acme.transport.terminal.dwell.dwellnotice.DwellRequest
import com.acme.transport.terminal.dwell.dwellnotice.normalizeWeightedDwellNoticeCode

fun summarizeDwellRequestsForSchedule(items: MutableList<DwellRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveLenientDwellSchedulesFromRequest(items: MutableList<DwellRequest>): MutableList<LenientDwellSchedule> {
    val out = mutableListOf<LenientDwellSchedule>()
    for (item in items) {
        val derived = newLenientDwellSchedule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankLenientDwellSchedule(item: LenientDwellSchedule): String = normalizeWeightedDwellNoticeCode(item.code)
