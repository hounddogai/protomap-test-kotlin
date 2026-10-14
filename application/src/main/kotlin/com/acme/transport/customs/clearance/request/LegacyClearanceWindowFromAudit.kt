package com.acme.transport.customs.clearance.request

import com.acme.transport.customs.hold.audit.PendingHoldAudit
import com.acme.transport.customs.hold.audit.holdEstimateWeight

fun summarizePendingHoldAuditsForWindow(items: MutableList<PendingHoldAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyClearanceWindowsFromAudit(items: MutableList<PendingHoldAudit>): MutableList<LegacyClearanceWindow> {
    val out = mutableListOf<LegacyClearanceWindow>()
    for (item in items) {
        val derived = newLegacyClearanceWindow(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankLegacyClearanceWindow(item: LegacyClearanceWindow): Double = holdEstimateWeight(8, true)
