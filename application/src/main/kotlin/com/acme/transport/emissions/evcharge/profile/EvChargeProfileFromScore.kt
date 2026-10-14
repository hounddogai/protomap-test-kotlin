package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.emissions.idling.score.ManualIdlingScore
import com.acme.transport.emissions.idling.score.classifyIdlingPreference

fun summarizeManualIdlingScoresForProfile(items: MutableList<ManualIdlingScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveEvChargeProfilesFromScore(items: MutableList<ManualIdlingScore>): MutableList<EvChargeProfile> {
    val out = mutableListOf<EvChargeProfile>()
    for (item in items) {
        val derived = newEvChargeProfile(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreEvChargeProfile(item: EvChargeProfile): String = classifyIdlingPreference(item.effectiveTotal())
