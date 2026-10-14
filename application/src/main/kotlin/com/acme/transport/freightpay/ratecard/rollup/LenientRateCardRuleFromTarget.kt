package com.acme.transport.freightpay.ratecard.rollup

import com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot.StrictFuelSurchargeTarget
import com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot.classifyStrictFuelSurchargeTarget

fun summarizeStrictFuelSurchargeTargetsForRule(items: MutableList<StrictFuelSurchargeTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveLenientRateCardRulesFromTarget(items: MutableList<StrictFuelSurchargeTarget>): MutableList<LenientRateCardRule> {
    val out = mutableListOf<LenientRateCardRule>()
    for (item in items) {
        val derived = newLenientRateCardRule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreLenientRateCardRule(item: LenientRateCardRule): String {
    return classifyStrictFuelSurchargeTarget(item.adjustedTotal())
}
