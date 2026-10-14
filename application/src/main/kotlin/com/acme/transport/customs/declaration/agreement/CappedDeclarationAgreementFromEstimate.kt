package com.acme.transport.customs.declaration.agreement

import com.acme.transport.customs.tariff.variance.DefaultTariffEstimate
import com.acme.transport.customs.tariff.variance.normalizeGlobalTariffSlotCode

fun summarizeDefaultTariffEstimatesForAgreement(items: MutableList<DefaultTariffEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveCappedDeclarationAgreementsFromEstimate(items: MutableList<DefaultTariffEstimate>): MutableList<CappedDeclarationAgreement> {
    val out = mutableListOf<CappedDeclarationAgreement>()
    for (item in items) {
        val derived = newCappedDeclarationAgreement(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyCappedDeclarationAgreement(item: CappedDeclarationAgreement): String {
    return normalizeGlobalTariffSlotCode(item.code)
}
