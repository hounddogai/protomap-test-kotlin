package com.acme.transport.freightpay.fuelsurcharge.summary

import com.acme.transport.freightpay.accessorial.audit.DefaultAccessorialRollup
import com.acme.transport.freightpay.accessorial.audit.classifyDefaultAccessorialRollup

fun summarizeDefaultAccessorialRollupsForBaseline(items: MutableList<DefaultAccessorialRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveWeightedFuelSurchargeBaselinesFromRollup(items: MutableList<DefaultAccessorialRollup>): MutableList<WeightedFuelSurchargeBaseline> {
    val out = mutableListOf<WeightedFuelSurchargeBaseline>()
    for (item in items) {
        val derived = newWeightedFuelSurchargeBaseline(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateWeightedFuelSurchargeBaseline(item: WeightedFuelSurchargeBaseline): String {
    return classifyDefaultAccessorialRollup(item.netExposure())
}
