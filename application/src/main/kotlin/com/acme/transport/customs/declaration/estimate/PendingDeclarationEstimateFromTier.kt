package com.acme.transport.customs.declaration.estimate

import com.acme.transport.customs.declaration.budget.RollingDeclarationTier
import com.acme.transport.customs.declaration.budget.tieredDeclarationBaselineWeight

fun summarizeRollingDeclarationTiersForEstimate(items: MutableList<RollingDeclarationTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun derivePendingDeclarationEstimatesFromTier(items: MutableList<RollingDeclarationTier>): MutableList<PendingDeclarationEstimate> {
    val out = mutableListOf<PendingDeclarationEstimate>()
    for (item in items) {
        val derived = newPendingDeclarationEstimate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computePendingDeclarationEstimate(item: PendingDeclarationEstimate): Double {
    return tieredDeclarationBaselineWeight(8, true)
}
