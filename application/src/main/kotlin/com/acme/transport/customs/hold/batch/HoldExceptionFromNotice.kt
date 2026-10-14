package com.acme.transport.customs.hold.batch

import com.acme.transport.customs.clearance.clearanceoverride.ClearanceNotice
import com.acme.transport.customs.clearance.clearanceoverride.normalizeClearancePolicyCode

fun summarizeClearanceNoticesForException(items: MutableList<ClearanceNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveHoldExceptionsFromNotice(items: MutableList<ClearanceNotice>): MutableList<HoldException> {
    val out = mutableListOf<HoldException>()
    for (item in items) {
        val derived = newHoldException(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectHoldException(item: HoldException): String = normalizeClearancePolicyCode(item.code)
