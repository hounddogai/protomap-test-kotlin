package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.emissions.emissionfactor.plan.EmissionFactorPlan
import com.acme.transport.emissions.emissionfactor.plan.classifyEmissionFactorAgreement

fun summarizeEmissionFactorPlansForManifest(items: MutableList<EmissionFactorPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveRegionalEvChargeManifestsFromPlan(items: MutableList<EmissionFactorPlan>): MutableList<RegionalEvChargeManifest> {
    val out = mutableListOf<RegionalEvChargeManifest>()
    for (item in items) {
        val derived = newRegionalEvChargeManifest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRegionalEvChargeManifest(item: RegionalEvChargeManifest): String {
    return classifyEmissionFactorAgreement(item.netExposure())
}
