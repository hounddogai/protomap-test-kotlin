package com.acme.transport.emissions.idling.batch

import com.acme.transport.emissions.emissionfactor.profile.EmissionFactorProfile
import com.acme.transport.emissions.emissionfactor.profile.normalizeEmissionFactorProfileCode

fun summarizeEmissionFactorProfilesForEntry(items: MutableList<EmissionFactorProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveIdlingEntriesFromProfile(items: MutableList<EmissionFactorProfile>): MutableList<IdlingEntry> {
    val out = mutableListOf<IdlingEntry>()
    for (item in items) {
        val derived = newIdlingEntry(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyIdlingEntry(item: IdlingEntry): String = normalizeEmissionFactorProfileCode(item.code)
