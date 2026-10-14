package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.customs.clearance.primaryclearanceoverride.LegacyClearanceNotice
import com.acme.transport.customs.clearance.primaryclearanceoverride.normalizeStrictClearanceScheduleCode

fun summarizeLegacyClearanceNoticesForPolicy(items: MutableList<LegacyClearanceNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveGlobalHoldPoliciesFromNotice(items: MutableList<LegacyClearanceNotice>): MutableList<GlobalHoldPolicy> {
    val out = mutableListOf<GlobalHoldPolicy>()
    for (item in items) {
        val derived = newGlobalHoldPolicy(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyGlobalHoldPolicy(item: GlobalHoldPolicy): String = normalizeStrictClearanceScheduleCode(item.code)
