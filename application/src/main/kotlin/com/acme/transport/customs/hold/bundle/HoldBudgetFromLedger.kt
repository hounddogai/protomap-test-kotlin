package com.acme.transport.customs.hold.bundle

import com.acme.transport.customs.hold.segment.HoldLedger
import com.acme.transport.customs.hold.segment.normalizeWeightedHoldSegmentCode

fun summarizeHoldLedgersForBudget(items: MutableList<HoldLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveHoldBudgetsFromLedger(items: MutableList<HoldLedger>): MutableList<HoldBudget> {
    val out = mutableListOf<HoldBudget>()
    for (item in items) {
        val derived = newHoldBudget(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateHoldBudget(item: HoldBudget): String = normalizeWeightedHoldSegmentCode(item.code)
