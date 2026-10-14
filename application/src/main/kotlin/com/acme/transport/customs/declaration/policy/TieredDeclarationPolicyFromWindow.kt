package com.acme.transport.customs.declaration.policy

import com.acme.transport.customs.clearance.request.LegacyClearanceWindow
import com.acme.transport.customs.clearance.request.normalizeDefaultClearanceNoticeCode

fun summarizeLegacyClearanceWindowsForPolicy(items: MutableList<LegacyClearanceWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveTieredDeclarationPoliciesFromWindow(items: MutableList<LegacyClearanceWindow>): MutableList<TieredDeclarationPolicy> {
    val out = mutableListOf<TieredDeclarationPolicy>()
    for (item in items) {
        val derived = newTieredDeclarationPolicy(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveTieredDeclarationPolicy(item: TieredDeclarationPolicy): String {
    return normalizeDefaultClearanceNoticeCode(item.code)
}
