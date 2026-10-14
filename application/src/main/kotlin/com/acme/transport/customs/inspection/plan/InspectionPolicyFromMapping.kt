package com.acme.transport.customs.inspection.plan

import com.acme.transport.customs.declaration.forecast.DeclarationMapping
import com.acme.transport.customs.declaration.forecast.declarationCycleBucket

fun summarizeDeclarationMappingsForPolicy(items: MutableList<DeclarationMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveInspectionPoliciesFromMapping(items: MutableList<DeclarationMapping>): MutableList<InspectionPolicy> {
    val out = mutableListOf<InspectionPolicy>()
    for (item in items) {
        val derived = newInspectionPolicy(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateInspectionPolicy(item: InspectionPolicy): String = declarationCycleBucket(item.measuredValue())
