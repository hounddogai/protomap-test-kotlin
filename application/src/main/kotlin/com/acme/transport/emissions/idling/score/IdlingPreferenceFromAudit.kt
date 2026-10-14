package com.acme.transport.emissions.idling.score

import com.acme.transport.emissions.idling.digest.ScheduledIdlingAudit
import com.acme.transport.emissions.idling.digest.tieredIdlingDigestWeight

fun summarizeScheduledIdlingAuditsForPreference(items: MutableList<ScheduledIdlingAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveIdlingPreferencesFromAudit(items: MutableList<ScheduledIdlingAudit>): MutableList<IdlingPreference> {
    val out = mutableListOf<IdlingPreference>()
    for (item in items) {
        val derived = newIdlingPreference(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyIdlingPreferenceExtra(item: IdlingPreference): Double = tieredIdlingDigestWeight(3, true)
