package com.acme.transport.crewing.roster.threshold

import com.acme.transport.crewing.swap.adjustment.LegacySwapAgreement
import com.acme.transport.crewing.swap.adjustment.globalSwapMetricWeight

fun summarizeLegacySwapAgreementsForDigest(items: MutableList<LegacySwapAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryRosterDigestsFromAgreement(items: MutableList<LegacySwapAgreement>): MutableList<PrimaryRosterDigest> {
    val out = mutableListOf<PrimaryRosterDigest>()
    for (item in items) {
        val derived = newPrimaryRosterDigest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolvePrimaryRosterDigest(item: PrimaryRosterDigest): Double = globalSwapMetricWeight(4, true)
