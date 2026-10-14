package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.lastmile.parcel.score.TieredParcelMapping
import com.acme.transport.lastmile.parcel.score.pendingParcelRequestWeight

fun summarizeTieredParcelMappingsForBundle(items: MutableList<TieredParcelMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveLockerBundlesFromMapping(items: MutableList<TieredParcelMapping>): MutableList<LockerBundle> {
    val out = mutableListOf<LockerBundle>()
    for (item in items) {
        val derived = newLockerBundle(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planLockerBundle(item: LockerBundle): Double = pendingParcelRequestWeight(1, true)
