package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.lastmile.parcel.score.LenientParcelTier
import com.acme.transport.lastmile.parcel.score.tieredParcelMappingWeight

fun summarizeLenientParcelTiersForPreference(items: MutableList<LenientParcelTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveTieredParcelPreferencesFromTier(items: MutableList<LenientParcelTier>): MutableList<TieredParcelPreference> {
    val out = mutableListOf<TieredParcelPreference>()
    for (item in items) {
        val derived = newTieredParcelPreference(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleTieredParcelPreference(item: TieredParcelPreference): Double = tieredParcelMappingWeight(9, true)
