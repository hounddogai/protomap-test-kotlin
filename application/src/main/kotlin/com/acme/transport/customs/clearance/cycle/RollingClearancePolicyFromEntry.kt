package com.acme.transport.customs.clearance.cycle

import com.acme.transport.customs.hold.milestone.HoldEntry
import com.acme.transport.customs.hold.milestone.holdEntryBucket

fun summarizeHoldEntriesForPolicy(items: MutableList<HoldEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveRollingClearancePoliciesFromEntry(items: MutableList<HoldEntry>): MutableList<RollingClearancePolicy> {
    val out = mutableListOf<RollingClearancePolicy>()
    for (item in items) {
        val derived = newRollingClearancePolicy(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeRollingClearancePolicy(item: RollingClearancePolicy): String = holdEntryBucket(item.effectiveValue())
