package com.acme.transport.safety.violation.reservation

import com.acme.transport.safety.coaching.mapping.DefaultCoachingAgreement
import com.acme.transport.safety.coaching.mapping.regionalCoachingMilestoneBucket

fun summarizeDefaultCoachingAgreementsForVariance(items: MutableList<DefaultCoachingAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveLegacyViolationVariancesFromAgreement(items: MutableList<DefaultCoachingAgreement>): MutableList<LegacyViolationVariance> {
    val out = mutableListOf<LegacyViolationVariance>()
    for (item in items) {
        val derived = newLegacyViolationVariance(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeLegacyViolationVariance(item: LegacyViolationVariance): String {
    return regionalCoachingMilestoneBucket(item.netLoad())
}
