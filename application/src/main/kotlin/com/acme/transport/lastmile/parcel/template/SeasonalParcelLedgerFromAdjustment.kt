package com.acme.transport.lastmile.parcel.template

import com.acme.transport.lastmile.proofofdelivery.deliveryallocation.ManualProofOfDeliveryAdjustment
import com.acme.transport.lastmile.proofofdelivery.deliveryallocation.classifyManualProofOfDeliveryAdjustment

fun summarizeManualProofOfDeliveryAdjustmentsForLedger(items: MutableList<ManualProofOfDeliveryAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveSeasonalParcelLedgersFromAdjustment(items: MutableList<ManualProofOfDeliveryAdjustment>): MutableList<SeasonalParcelLedger> {
    val out = mutableListOf<SeasonalParcelLedger>()
    for (item in items) {
        val derived = newSeasonalParcelLedger(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleSeasonalParcelLedger(item: SeasonalParcelLedger): String {
    return classifyManualProofOfDeliveryAdjustment(item.weightedVolume())
}
