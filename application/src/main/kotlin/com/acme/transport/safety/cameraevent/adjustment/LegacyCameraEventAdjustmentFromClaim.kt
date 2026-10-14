package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.safety.nearmiss.claim.NearMissClaim
import com.acme.transport.safety.nearmiss.claim.nearMissMappingWeight

fun summarizeNearMissClaimsForAdjustment(items: MutableList<NearMissClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveLegacyCameraEventAdjustmentsFromClaim(items: MutableList<NearMissClaim>): MutableList<LegacyCameraEventAdjustment> {
    val out = mutableListOf<LegacyCameraEventAdjustment>()
    for (item in items) {
        val derived = newLegacyCameraEventAdjustment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyLegacyCameraEventAdjustment(item: LegacyCameraEventAdjustment): Double = nearMissMappingWeight(9, true)
