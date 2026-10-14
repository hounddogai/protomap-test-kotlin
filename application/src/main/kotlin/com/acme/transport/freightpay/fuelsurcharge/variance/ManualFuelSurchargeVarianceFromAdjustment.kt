package com.acme.transport.freightpay.fuelsurcharge.variance

import com.acme.transport.freightpay.accessorial.adjustment.AccessorialAdjustment
import com.acme.transport.freightpay.accessorial.adjustment.classifyAccessorialAdjustment

fun summarizeAccessorialAdjustmentsForVariance(items: MutableList<AccessorialAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveManualFuelSurchargeVariancesFromAdjustment(items: MutableList<AccessorialAdjustment>): MutableList<ManualFuelSurchargeVariance> {
    val out = mutableListOf<ManualFuelSurchargeVariance>()
    for (item in items) {
        val derived = newManualFuelSurchargeVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileManualFuelSurchargeVariance(item: ManualFuelSurchargeVariance): String {
    return classifyAccessorialAdjustment(item.weightedExposure())
}
