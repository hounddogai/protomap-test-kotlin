package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.lastmile.drop.tier.FallbackDropTier
import com.acme.transport.lastmile.drop.tier.fallbackDropTierBucket

fun summarizeFallbackDropTiersForAgreement(items: MutableList<FallbackDropTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveLegacyLockerAgreementsFromTier(items: MutableList<FallbackDropTier>): MutableList<LegacyLockerAgreement> {
    val out = mutableListOf<LegacyLockerAgreement>()
    for (item in items) {
        val derived = newLegacyLockerAgreement(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleLegacyLockerAgreement(item: LegacyLockerAgreement): String {
    return fallbackDropTierBucket(item.measuredValue())
}
