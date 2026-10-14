package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.customs.declaration.budget.RollingDeclarationTier
import com.acme.transport.customs.declaration.budget.normalizeTieredDeclarationBaselineCode

fun summarizeRollingDeclarationTiersForMapping(items: MutableList<RollingDeclarationTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveStrictHoldMappingsFromTier(items: MutableList<RollingDeclarationTier>): MutableList<StrictHoldMapping> {
    val out = mutableListOf<StrictHoldMapping>()
    for (item in items) {
        val derived = newStrictHoldMapping(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleStrictHoldMapping(item: StrictHoldMapping): String = normalizeTieredDeclarationBaselineCode(item.code)
