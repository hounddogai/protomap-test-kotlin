package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.freightpay.audit.lenientauditproposal.AuditTier
import com.acme.transport.freightpay.audit.lenientauditproposal.classifyAdaptiveAuditRecord

fun summarizeAuditTiersForForecast(items: MutableList<AuditTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveGlobalFuelSurchargeForecastsFromTier(items: MutableList<AuditTier>): MutableList<GlobalFuelSurchargeForecast> {
    val out = mutableListOf<GlobalFuelSurchargeForecast>()
    for (item in items) {
        val derived = newGlobalFuelSurchargeForecast(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignGlobalFuelSurchargeForecast(item: GlobalFuelSurchargeForecast): String {
    return classifyAdaptiveAuditRecord(item.netTotal())
}
