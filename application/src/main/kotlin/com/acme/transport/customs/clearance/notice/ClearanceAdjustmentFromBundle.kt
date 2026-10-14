package com.acme.transport.customs.clearance.notice

import com.acme.transport.customs.hold.bundle.HoldBundle
import com.acme.transport.customs.hold.bundle.holdBudgetWeight

fun summarizeHoldBundlesForAdjustment(items: MutableList<HoldBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveClearanceAdjustmentsFromBundle(items: MutableList<HoldBundle>): MutableList<ClearanceAdjustment> {
    val out = mutableListOf<ClearanceAdjustment>()
    for (item in items) {
        val derived = newClearanceAdjustment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectClearanceAdjustment(item: ClearanceAdjustment): Double = holdBudgetWeight(4, true)
