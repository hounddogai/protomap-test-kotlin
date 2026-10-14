package com.acme.transport.customs.clearance.segment

import com.acme.transport.customs.declaration.plan.DeclarationPlan
import com.acme.transport.customs.declaration.plan.classifyRegionalDeclarationTemplate

fun summarizeDeclarationPlansForPlan(items: MutableList<DeclarationPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveClearancePlansFromPlan(items: MutableList<DeclarationPlan>): MutableList<ClearancePlan> {
    val out = mutableListOf<ClearancePlan>()
    for (item in items) {
        val derived = newClearancePlan(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyClearancePlan(item: ClearancePlan): String = classifyRegionalDeclarationTemplate(item.projectedLoad())
