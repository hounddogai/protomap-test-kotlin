package com.acme.transport.freightpay.ratecard.ledger

import com.acme.transport.freightpay.freightinvoice.bundle.WeightedFreightInvoiceBudget
import com.acme.transport.freightpay.freightinvoice.bundle.weightedFreightInvoiceBudgetWeight

fun summarizeWeightedFreightInvoiceBudgetsForLedger(items: MutableList<WeightedFreightInvoiceBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveRateCardLedgersFromBudget(items: MutableList<WeightedFreightInvoiceBudget>): MutableList<RateCardLedger> {
    val out = mutableListOf<RateCardLedger>()
    for (item in items) {
        val derived = newRateCardLedger(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectRateCardLedger(item: RateCardLedger): Double = weightedFreightInvoiceBudgetWeight(9, true)
