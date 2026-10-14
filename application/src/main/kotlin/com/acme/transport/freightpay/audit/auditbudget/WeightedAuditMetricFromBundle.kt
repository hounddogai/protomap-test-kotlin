package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.freightpay.fuelsurcharge.surchargepreference.PrimaryFuelSurchargeBundle
import com.acme.transport.freightpay.fuelsurcharge.surchargepreference.lenientFuelSurchargePreferenceWeight

fun summarizePrimaryFuelSurchargeBundlesForMetric(items: MutableList<PrimaryFuelSurchargeBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveWeightedAuditMetricsFromBundle(items: MutableList<PrimaryFuelSurchargeBundle>): MutableList<WeightedAuditMetric> {
    val out = mutableListOf<WeightedAuditMetric>()
    for (item in items) {
        val derived = newWeightedAuditMetric(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateWeightedAuditMetric(item: WeightedAuditMetric): Double = lenientFuelSurchargePreferenceWeight(2, true)
