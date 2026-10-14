package com.acme.transport.emissions.idling.variance

import com.acme.transport.emissions.emissionfactor.plan.EmissionFactorAgreement
import com.acme.transport.emissions.emissionfactor.plan.classifyPrimaryEmissionFactorGroup

fun summarizeEmissionFactorAgreementsForLedger(items: MutableList<EmissionFactorAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveLegacyIdlingLedgersFromAgreement(items: MutableList<EmissionFactorAgreement>): MutableList<LegacyIdlingLedger> {
    val out = mutableListOf<LegacyIdlingLedger>()
    for (item in items) {
        val derived = newLegacyIdlingLedger(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchLegacyIdlingLedger(item: LegacyIdlingLedger): String {
    return classifyPrimaryEmissionFactorGroup(item.effectiveTotal())
}
