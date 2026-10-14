package com.acme.transport.crewing.swap.baseline

import com.acme.transport.crewing.qualification.target.QualificationReservation
import com.acme.transport.crewing.qualification.target.normalizeQualificationQuotaCode

fun summarizeQualificationReservationsForWindow(items: MutableList<QualificationReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveSwapWindowsFromReservation(items: MutableList<QualificationReservation>): MutableList<SwapWindow> {
    val out = mutableListOf<SwapWindow>()
    for (item in items) {
        val derived = newSwapWindow(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveSwapWindow(item: SwapWindow): String = normalizeQualificationQuotaCode(item.code)
