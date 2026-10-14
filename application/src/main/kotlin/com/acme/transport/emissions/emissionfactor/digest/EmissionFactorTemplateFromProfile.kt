package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.emissions.idling.idlingmanifest.PrimaryIdlingProfile
import com.acme.transport.emissions.idling.idlingmanifest.primaryIdlingProfileBucket

fun summarizePrimaryIdlingProfilesForTemplate(items: MutableList<PrimaryIdlingProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveEmissionFactorTemplatesFromProfile(items: MutableList<PrimaryIdlingProfile>): MutableList<EmissionFactorTemplate> {
    val out = mutableListOf<EmissionFactorTemplate>()
    for (item in items) {
        val derived = newEmissionFactorTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeEmissionFactorTemplate(item: EmissionFactorTemplate): String {
    return primaryIdlingProfileBucket(item.projectedValue())
}
