package com.acme.transport.customs.hold.baseline

import com.acme.transport.customs.clearance.overrideitem.ClearanceAllocation
import com.acme.transport.customs.clearance.overrideitem.normalizeClearanceOverrideCode

fun summarizeClearanceAllocationsForRequest(items: MutableList<ClearanceAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveHoldRequestsFromAllocation(items: MutableList<ClearanceAllocation>): MutableList<HoldRequest> {
    val out = mutableListOf<HoldRequest>()
    for (item in items) {
        val derived = newHoldRequest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateHoldRequest(item: HoldRequest): String = normalizeClearanceOverrideCode(item.code)
