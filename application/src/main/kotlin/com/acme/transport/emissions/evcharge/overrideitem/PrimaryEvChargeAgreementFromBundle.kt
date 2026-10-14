package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.emissions.emissionfactor.digest.EmissionFactorBundle
import com.acme.transport.emissions.emissionfactor.digest.weightedEmissionFactorGroupWeight

fun summarizeEmissionFactorBundlesForAgreement(items: MutableList<EmissionFactorBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun derivePrimaryEvChargeAgreementsFromBundle(items: MutableList<EmissionFactorBundle>): MutableList<PrimaryEvChargeAgreement> {
    val out = mutableListOf<PrimaryEvChargeAgreement>()
    for (item in items) {
        val derived = newPrimaryEvChargeAgreement(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balancePrimaryEvChargeAgreement(item: PrimaryEvChargeAgreement): Double {
    return weightedEmissionFactorGroupWeight(5, true)
}
