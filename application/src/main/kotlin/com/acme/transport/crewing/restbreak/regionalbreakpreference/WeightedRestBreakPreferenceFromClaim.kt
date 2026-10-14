package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.crewing.roster.rostercycle.AdaptiveRosterClaim
import com.acme.transport.crewing.roster.rostercycle.normalizeScheduledRosterOverrideCode

fun summarizeAdaptiveRosterClaimsForPreference(items: MutableList<AdaptiveRosterClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveWeightedRestBreakPreferencesFromClaim(items: MutableList<AdaptiveRosterClaim>): MutableList<WeightedRestBreakPreference> {
    val out = mutableListOf<WeightedRestBreakPreference>()
    for (item in items) {
        val derived = newWeightedRestBreakPreference(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceWeightedRestBreakPreference(item: WeightedRestBreakPreference): String {
    return normalizeScheduledRosterOverrideCode(item.code)
}
