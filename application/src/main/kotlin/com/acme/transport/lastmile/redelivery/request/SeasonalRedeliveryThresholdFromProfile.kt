package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.lastmile.locker.lockerprofile.LegacyLockerProfile
import com.acme.transport.lastmile.locker.lockerprofile.seasonalLockerSegmentWeight

fun summarizeLegacyLockerProfilesForThreshold(items: MutableList<LegacyLockerProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveSeasonalRedeliveryThresholdsFromProfile(items: MutableList<LegacyLockerProfile>): MutableList<SeasonalRedeliveryThreshold> {
    val out = mutableListOf<SeasonalRedeliveryThreshold>()
    for (item in items) {
        val derived = newSeasonalRedeliveryThreshold(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectSeasonalRedeliveryThreshold(item: SeasonalRedeliveryThreshold): Double {
    return seasonalLockerSegmentWeight(8, true)
}
