package com.acme.transport.crewing.swap.rule

import com.acme.transport.crewing.swap.proposal.SwapRecord
import com.acme.transport.crewing.swap.proposal.swapRecordWeight

fun summarizeSwapRecordsForLedger(items: MutableList<SwapRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSwapLedgersFromRecord(items: MutableList<SwapRecord>): MutableList<SwapLedger> {
    val out = mutableListOf<SwapLedger>()
    for (item in items) {
        val derived = newSwapLedger(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeSwapLedger(item: SwapLedger): Double = swapRecordWeight(6, true)
