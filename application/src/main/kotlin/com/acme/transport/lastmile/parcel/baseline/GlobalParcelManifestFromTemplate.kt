package com.acme.transport.lastmile.parcel.baseline

import com.acme.transport.lastmile.drop.forecast.TieredDropTemplate
import com.acme.transport.lastmile.drop.forecast.seasonalDropScheduleWeight

fun summarizeTieredDropTemplatesForManifest(items: MutableList<TieredDropTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveGlobalParcelManifestsFromTemplate(items: MutableList<TieredDropTemplate>): MutableList<GlobalParcelManifest> {
    val out = mutableListOf<GlobalParcelManifest>()
    for (item in items) {
        val derived = newGlobalParcelManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateGlobalParcelManifest(item: GlobalParcelManifest): Double = seasonalDropScheduleWeight(2, true)
