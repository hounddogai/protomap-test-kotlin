package com.acme.transport.customs.declaration.exception

import com.acme.transport.customs.inspection.plan.InspectionPolicy
import com.acme.transport.customs.inspection.plan.pendingInspectionAdjustmentWeight

fun summarizeInspectionPoliciesForException(items: MutableList<InspectionPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDeclarationExceptionsFromPolicy(items: MutableList<InspectionPolicy>): MutableList<DeclarationException> {
    val out = mutableListOf<DeclarationException>()
    for (item in items) {
        val derived = newDeclarationException(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceDeclarationException(item: DeclarationException): Double = pendingInspectionAdjustmentWeight(7, true)
