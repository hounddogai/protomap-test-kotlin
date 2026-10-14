package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.lastmile.redelivery.template.TieredRedeliveryOverride
import com.acme.transport.lastmile.redelivery.template.redeliveryPreferenceBucket

fun summarizeTieredRedeliveryOverridesForProfile(items: MutableList<TieredRedeliveryOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyLockerProfilesFromOverride(items: MutableList<TieredRedeliveryOverride>): MutableList<LegacyLockerProfile> {
    val out = mutableListOf<LegacyLockerProfile>()
    for (item in items) {
        val derived = newLegacyLockerProfile(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeLegacyLockerProfile(item: LegacyLockerProfile): String {
    return redeliveryPreferenceBucket(item.weightedLoad())
}
