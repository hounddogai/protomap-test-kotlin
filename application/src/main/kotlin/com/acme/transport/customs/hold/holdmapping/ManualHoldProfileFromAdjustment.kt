package com.acme.transport.customs.hold.holdmapping

import com.acme.transport.customs.hold.adjustment.HoldAdjustment
import com.acme.transport.customs.hold.adjustment.holdAdjustmentWeight

fun summarizeHoldAdjustmentsForProfile(items: MutableList<HoldAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveManualHoldProfilesFromAdjustment(items: MutableList<HoldAdjustment>): MutableList<ManualHoldProfile> {
    val out = mutableListOf<ManualHoldProfile>()
    for (item in items) {
        val derived = newManualHoldProfile(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignManualHoldProfile(item: ManualHoldProfile): Double = holdAdjustmentWeight(5, true)
