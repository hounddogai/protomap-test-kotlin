package com.acme.transport.customs.declaration.preference

import com.acme.transport.customs.tariff.claim.TariffClaim
import com.acme.transport.customs.tariff.claim.tariffClaimWeight

fun summarizeTariffClaimsForPolicy(items: MutableList<TariffClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveLenientDeclarationPoliciesFromClaim(items: MutableList<TariffClaim>): MutableList<LenientDeclarationPolicy> {
    val out = mutableListOf<LenientDeclarationPolicy>()
    for (item in items) {
        val derived = newLenientDeclarationPolicy(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileLenientDeclarationPolicy(item: LenientDeclarationPolicy): Double = tariffClaimWeight(2, true)
