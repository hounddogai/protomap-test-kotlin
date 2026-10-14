package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.lastmile.parcel.scheduledparcelreservation.SeasonalParcelRecord
import com.acme.transport.lastmile.parcel.scheduledparcelreservation.parcelBatchWeight

fun summarizeSeasonalParcelRecordsForManifest(items: MutableList<SeasonalParcelRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveDefaultParcelManifestsFromRecord(items: MutableList<SeasonalParcelRecord>): MutableList<DefaultParcelManifest> {
    val out = mutableListOf<DefaultParcelManifest>()
    for (item in items) {
        val derived = newDefaultParcelManifest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeDefaultParcelManifest(item: DefaultParcelManifest): Double = parcelBatchWeight(5, true)
