package com.acme.transport.customs.declaration.audit

import com.acme.transport.customs.tariff.checkpoint.TariffBudget
import com.acme.transport.customs.tariff.checkpoint.normalizeTariffCheckpointCode

fun summarizeTariffBudgetsForAudit(items: MutableList<TariffBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRegionalDeclarationAuditsFromBudget(items: MutableList<TariffBudget>): MutableList<RegionalDeclarationAudit> {
    val out = mutableListOf<RegionalDeclarationAudit>()
    for (item in items) {
        val derived = newRegionalDeclarationAudit(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeRegionalDeclarationAudit(item: RegionalDeclarationAudit): String {
    return normalizeTariffCheckpointCode(item.code)
}
