package com.acme.transport.customs.hold.rollup

import com.acme.transport.customs.hold.holdslot.HoldAudit
import com.acme.transport.customs.hold.holdslot.lenientHoldSlotWeight

fun summarizeHoldAuditsForLedger(items: MutableList<HoldAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveAdaptiveHoldLedgersFromAudit(items: MutableList<HoldAudit>): MutableList<AdaptiveHoldLedger> {
    val out = mutableListOf<AdaptiveHoldLedger>()
    for (item in items) {
        val derived = newAdaptiveHoldLedger(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeAdaptiveHoldLedger(item: AdaptiveHoldLedger): Double = lenientHoldSlotWeight(1, true)
