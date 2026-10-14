package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.customs.declaration.audit.DeclarationAudit
import com.acme.transport.customs.declaration.audit.declarationVarianceWeight

fun summarizeDeclarationAuditsForTemplate(items: MutableList<DeclarationAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveClearanceTemplatesFromAudit(items: MutableList<DeclarationAudit>): MutableList<ClearanceTemplate> {
    val out = mutableListOf<ClearanceTemplate>()
    for (item in items) {
        val derived = newClearanceTemplate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeClearanceTemplate(item: ClearanceTemplate): Double = declarationVarianceWeight(1, true)
