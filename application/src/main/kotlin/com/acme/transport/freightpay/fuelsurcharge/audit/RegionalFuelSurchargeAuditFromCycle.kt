package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.freightpay.fuelsurcharge.segment.FuelSurchargeCycle
import com.acme.transport.freightpay.fuelsurcharge.segment.fuelSurchargeCycleBucket

fun summarizeFuelSurchargeCyclesForAudit(items: MutableList<FuelSurchargeCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveRegionalFuelSurchargeAuditsFromCycle(items: MutableList<FuelSurchargeCycle>): MutableList<RegionalFuelSurchargeAudit> {
    val out = mutableListOf<RegionalFuelSurchargeAudit>()
    for (item in items) {
        val derived = newRegionalFuelSurchargeAudit(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveRegionalFuelSurchargeAudit(item: RegionalFuelSurchargeAudit): String {
    return fuelSurchargeCycleBucket(item.weightedValue())
}
