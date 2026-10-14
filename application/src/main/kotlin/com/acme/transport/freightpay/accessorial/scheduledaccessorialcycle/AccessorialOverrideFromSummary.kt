package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.freightpay.fuelsurcharge.milestone.FuelSurchargeSummary
import com.acme.transport.freightpay.fuelsurcharge.milestone.normalizeFuelSurchargeSummaryCode

fun summarizeFuelSurchargeSummariesForOverride(items: MutableList<FuelSurchargeSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveAccessorialOverridesFromSummary(items: MutableList<FuelSurchargeSummary>): MutableList<AccessorialOverride> {
    val out = mutableListOf<AccessorialOverride>()
    for (item in items) {
        val derived = newAccessorialOverride(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeAccessorialOverride(item: AccessorialOverride): String = normalizeFuelSurchargeSummaryCode(item.code)
