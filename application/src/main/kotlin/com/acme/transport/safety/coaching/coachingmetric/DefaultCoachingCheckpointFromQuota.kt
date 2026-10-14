package com.acme.transport.safety.coaching.coachingmetric

import com.acme.transport.safety.violation.manualviolationquota.StrictViolationQuota
import com.acme.transport.safety.violation.manualviolationquota.normalizeFallbackViolationSlotCode

fun summarizeStrictViolationQuotasForCheckpoint(items: MutableList<StrictViolationQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveDefaultCoachingCheckpointsFromQuota(items: MutableList<StrictViolationQuota>): MutableList<DefaultCoachingCheckpoint> {
    val out = mutableListOf<DefaultCoachingCheckpoint>()
    for (item in items) {
        val derived = newDefaultCoachingCheckpoint(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeDefaultCoachingCheckpoint(item: DefaultCoachingCheckpoint): String {
    return normalizeFallbackViolationSlotCode(item.code)
}
