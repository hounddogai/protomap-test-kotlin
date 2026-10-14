package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.freightpay.ratecard.adjustment.ManualRateCardAdjustment
import com.acme.transport.freightpay.ratecard.adjustment.classifyManualRateCardAdjustment

fun summarizeManualRateCardAdjustmentsForDigest(items: MutableList<ManualRateCardAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSeasonalRateCardDigestsFromAdjustment(items: MutableList<ManualRateCardAdjustment>): MutableList<SeasonalRateCardDigest> {
    val out = mutableListOf<SeasonalRateCardDigest>()
    for (item in items) {
        val derived = newSeasonalRateCardDigest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitSeasonalRateCardDigest(item: SeasonalRateCardDigest): String {
    return classifyManualRateCardAdjustment(item.measuredValue())
}
