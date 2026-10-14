package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.lastmile.proofofdelivery.manifest.RollingProofOfDeliveryPreference
import com.acme.transport.lastmile.proofofdelivery.manifest.defaultProofOfDeliveryRuleBucket

fun summarizeRollingProofOfDeliveryPreferencesForSummary(items: MutableList<RollingProofOfDeliveryPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveLockerSummariesFromPreference(items: MutableList<RollingProofOfDeliveryPreference>): MutableList<LockerSummary> {
    val out = mutableListOf<LockerSummary>()
    for (item in items) {
        val derived = newLockerSummary(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeLockerSummary(item: LockerSummary): String = defaultProofOfDeliveryRuleBucket(item.weightedTotal())
