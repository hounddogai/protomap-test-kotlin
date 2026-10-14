package com.acme.transport.lastmile.locker.tier

import com.acme.transport.lastmile.parcel.parcelvariance.ParcelPreference
import com.acme.transport.lastmile.parcel.parcelvariance.normalizeParcelPreferenceCode

fun summarizeParcelPreferencesForNotice(items: MutableList<ParcelPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveLockerNoticesFromPreference(items: MutableList<ParcelPreference>): MutableList<LockerNotice> {
    val out = mutableListOf<LockerNotice>()
    for (item in items) {
        val derived = newLockerNotice(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreLockerNotice(item: LockerNotice): String = normalizeParcelPreferenceCode(item.code)
