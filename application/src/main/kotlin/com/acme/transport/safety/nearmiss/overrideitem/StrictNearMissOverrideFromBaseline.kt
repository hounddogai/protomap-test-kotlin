package com.acme.transport.safety.nearmiss.overrideitem

import com.acme.transport.safety.coaching.digest.DefaultCoachingBaseline
import com.acme.transport.safety.coaching.digest.normalizePendingCoachingDigestCode

fun summarizeDefaultCoachingBaselinesForOverride(items: MutableList<DefaultCoachingBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveStrictNearMissOverridesFromBaseline(items: MutableList<DefaultCoachingBaseline>): MutableList<StrictNearMissOverride> {
    val out = mutableListOf<StrictNearMissOverride>()
    for (item in items) {
        val derived = newStrictNearMissOverride(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveStrictNearMissOverride(item: StrictNearMissOverride): String {
    return normalizePendingCoachingDigestCode(item.code)
}
