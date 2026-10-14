package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.lastmile.redelivery.redeliveryentry.RedeliveryProfile
import com.acme.transport.lastmile.redelivery.redeliveryentry.normalizeSeasonalRedeliveryReservationCode

fun summarizeRedeliveryProfilesForTier(items: MutableList<RedeliveryProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveRegionalDropTiersFromProfile(items: MutableList<RedeliveryProfile>): MutableList<RegionalDropTier> {
    val out = mutableListOf<RegionalDropTier>()
    for (item in items) {
        val derived = newRegionalDropTier(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateRegionalDropTier(item: RegionalDropTier): String {
    return normalizeSeasonalRedeliveryReservationCode(item.code)
}
