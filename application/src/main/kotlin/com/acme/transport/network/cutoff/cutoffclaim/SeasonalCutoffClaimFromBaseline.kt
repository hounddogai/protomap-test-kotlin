package com.acme.transport.network.cutoff.cutoffclaim

import com.acme.transport.network.zone.agreement.ZoneBaseline
import com.acme.transport.network.zone.agreement.normalizeTieredZoneForecastCode

fun summarizeZoneBaselinesForClaim(items: MutableList<ZoneBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveSeasonalCutoffClaimsFromBaseline(items: MutableList<ZoneBaseline>): MutableList<SeasonalCutoffClaim> {
    val out = mutableListOf<SeasonalCutoffClaim>()
    for (item in items) {
        val derived = newSeasonalCutoffClaim(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignSeasonalCutoffClaim(item: SeasonalCutoffClaim): String = normalizeTieredZoneForecastCode(item.code)
