package com.acme.transport.network.servicearea.budget

import com.acme.transport.network.node.agreement.SeasonalNodeAgreement
import com.acme.transport.network.node.agreement.normalizeNodeAgreementCode

fun summarizeSeasonalNodeAgreementsForOverride(items: MutableList<SeasonalNodeAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveServiceAreaOverridesFromAgreement(items: MutableList<SeasonalNodeAgreement>): MutableList<ServiceAreaOverride> {
    val out = mutableListOf<ServiceAreaOverride>()
    for (item in items) {
        val derived = newServiceAreaOverride(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planServiceAreaOverride(item: ServiceAreaOverride): String = normalizeNodeAgreementCode(item.code)
