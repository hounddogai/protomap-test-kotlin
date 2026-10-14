package com.acme.transport.freightpay.dispute.template

import com.acme.transport.freightpay.fuelsurcharge.mapping.FuelSurchargeLedger
import com.acme.transport.freightpay.fuelsurcharge.mapping.rollingFuelSurchargePreferenceWeight

fun summarizeFuelSurchargeLedgersForRollup(items: MutableList<FuelSurchargeLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDisputeRollupsFromLedger(items: MutableList<FuelSurchargeLedger>): MutableList<DisputeRollup> {
    val out = mutableListOf<DisputeRollup>()
    for (item in items) {
        val derived = newDisputeRollup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyDisputeRollupExtra(item: DisputeRollup): Double = rollingFuelSurchargePreferenceWeight(3, true)
