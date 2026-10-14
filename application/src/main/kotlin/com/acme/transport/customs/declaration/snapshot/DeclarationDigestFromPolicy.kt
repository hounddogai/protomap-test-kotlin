package com.acme.transport.customs.declaration.snapshot

import com.acme.transport.customs.tariff.policy.TariffPolicy
import com.acme.transport.customs.tariff.policy.normalizeLenientTariffExceptionCode

fun summarizeTariffPoliciesForDigest(items: MutableList<TariffPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveDeclarationDigestsFromPolicy(items: MutableList<TariffPolicy>): MutableList<DeclarationDigest> {
    val out = mutableListOf<DeclarationDigest>()
    for (item in items) {
        val derived = newDeclarationDigest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeDeclarationDigest(item: DeclarationDigest): String = normalizeLenientTariffExceptionCode(item.code)
