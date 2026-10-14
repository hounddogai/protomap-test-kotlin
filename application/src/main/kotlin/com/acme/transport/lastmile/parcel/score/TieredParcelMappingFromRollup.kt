package com.acme.transport.lastmile.parcel.score

import com.acme.transport.lastmile.parcel.parcelcycle.LenientParcelRollup
import com.acme.transport.lastmile.parcel.parcelcycle.defaultParcelManifestWeight

fun summarizeLenientParcelRollupsForMapping(items: MutableList<LenientParcelRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveTieredParcelMappingsFromRollup(items: MutableList<LenientParcelRollup>): MutableList<TieredParcelMapping> {
    val out = mutableListOf<TieredParcelMapping>()
    for (item in items) {
        val derived = newTieredParcelMapping(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceTieredParcelMapping(item: TieredParcelMapping): Double = defaultParcelManifestWeight(6, true)
