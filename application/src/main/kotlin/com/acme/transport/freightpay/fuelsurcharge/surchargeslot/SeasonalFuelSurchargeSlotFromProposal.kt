package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import com.acme.transport.freightpay.ratecard.score.RateCardProposal
import com.acme.transport.freightpay.ratecard.score.classifyDefaultRateCardPolicy

fun summarizeRateCardProposalsForSlot(items: MutableList<RateCardProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveSeasonalFuelSurchargeSlotsFromProposal(items: MutableList<RateCardProposal>): MutableList<SeasonalFuelSurchargeSlot> {
    val out = mutableListOf<SeasonalFuelSurchargeSlot>()
    for (item in items) {
        val derived = newSeasonalFuelSurchargeSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeSeasonalFuelSurchargeSlot(item: SeasonalFuelSurchargeSlot): String {
    return classifyDefaultRateCardPolicy(item.projectedValue())
}
