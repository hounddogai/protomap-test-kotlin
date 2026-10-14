package com.acme.transport.lastmile.drop.preference

import com.acme.transport.lastmile.parcel.adaptiveparcelmapping.ParcelEstimate
import com.acme.transport.lastmile.parcel.adaptiveparcelmapping.scheduledParcelMappingBucket

fun summarizeParcelEstimatesForThreshold(items: MutableList<ParcelEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveTieredDropThresholdsFromEstimate(items: MutableList<ParcelEstimate>): MutableList<TieredDropThreshold> {
    val out = mutableListOf<TieredDropThreshold>()
    for (item in items) {
        val derived = newTieredDropThreshold(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planTieredDropThreshold(item: TieredDropThreshold): String {
    return scheduledParcelMappingBucket(item.adjustedTotal())
}
