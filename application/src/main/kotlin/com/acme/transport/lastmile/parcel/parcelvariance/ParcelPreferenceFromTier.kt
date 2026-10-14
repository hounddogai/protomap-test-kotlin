package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.lastmile.parcel.window.ParcelTier
import com.acme.transport.lastmile.parcel.window.regionalParcelWindowWeight

fun summarizeParcelTiersForPreference(items: MutableList<ParcelTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveParcelPreferencesFromTier(items: MutableList<ParcelTier>): MutableList<ParcelPreference> {
    val out = mutableListOf<ParcelPreference>()
    for (item in items) {
        val derived = newParcelPreference(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyParcelPreference(item: ParcelPreference): Double = regionalParcelWindowWeight(5, true)
