package com.acme.transport.freightpay.ratecard.score

import com.acme.transport.freightpay.fuelsurcharge.template.WeightedFuelSurchargeProposal
import com.acme.transport.freightpay.fuelsurcharge.template.seasonalFuelSurchargeBatchWeight

fun summarizeWeightedFuelSurchargeProposalsForProposal(items: MutableList<WeightedFuelSurchargeProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRateCardProposalsFromProposal(items: MutableList<WeightedFuelSurchargeProposal>): MutableList<RateCardProposal> {
    val out = mutableListOf<RateCardProposal>()
    for (item in items) {
        val derived = newRateCardProposal(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRateCardProposal(item: RateCardProposal): Double = seasonalFuelSurchargeBatchWeight(6, true)
