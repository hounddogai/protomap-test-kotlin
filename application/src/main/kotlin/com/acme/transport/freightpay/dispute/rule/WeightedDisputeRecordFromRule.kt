package com.acme.transport.freightpay.dispute.rule

import com.acme.transport.freightpay.fuelsurcharge.surchargerule.TieredFuelSurchargeRule
import com.acme.transport.freightpay.fuelsurcharge.surchargerule.classifyFuelSurchargeScore

fun summarizeTieredFuelSurchargeRulesForRecord(items: MutableList<TieredFuelSurchargeRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveWeightedDisputeRecordsFromRule(items: MutableList<TieredFuelSurchargeRule>): MutableList<WeightedDisputeRecord> {
    val out = mutableListOf<WeightedDisputeRecord>()
    for (item in items) {
        val derived = newWeightedDisputeRecord(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitWeightedDisputeRecord(item: WeightedDisputeRecord): String {
    return classifyFuelSurchargeScore(item.weightedLoad())
}
