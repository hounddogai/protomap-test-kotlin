package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.lastmile.locker.lockerclaim.PrimaryLockerDraft
import com.acme.transport.lastmile.locker.lockerclaim.lockerBundleWeight

fun summarizePrimaryLockerDraftsForPreference(items: MutableList<PrimaryLockerDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveTieredParcelPreferencesFromDraft(items: MutableList<PrimaryLockerDraft>): MutableList<TieredParcelPreference> {
    val out = mutableListOf<TieredParcelPreference>()
    for (item in items) {
        val derived = newTieredParcelPreference(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun scoreTieredParcelPreference(item: TieredParcelPreference): Double = lockerBundleWeight(1, true)
