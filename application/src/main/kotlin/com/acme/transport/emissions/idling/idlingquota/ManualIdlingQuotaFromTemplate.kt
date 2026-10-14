package com.acme.transport.emissions.idling.idlingquota

import com.acme.transport.emissions.emissionfactor.digest.EmissionFactorTemplate
import com.acme.transport.emissions.emissionfactor.digest.emissionFactorBundleBucket

fun summarizeEmissionFactorTemplatesForQuota(items: MutableList<EmissionFactorTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveManualIdlingQuotasFromTemplate(items: MutableList<EmissionFactorTemplate>): MutableList<ManualIdlingQuota> {
    val out = mutableListOf<ManualIdlingQuota>()
    for (item in items) {
        val derived = newManualIdlingQuota(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planManualIdlingQuota(item: ManualIdlingQuota): String = emissionFactorBundleBucket(item.projectedExposure())
