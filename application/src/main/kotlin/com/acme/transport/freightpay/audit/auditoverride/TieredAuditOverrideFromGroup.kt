package com.acme.transport.freightpay.audit.auditoverride

import com.acme.transport.freightpay.fuelsurcharge.milestone.RegionalFuelSurchargeGroup
import com.acme.transport.freightpay.fuelsurcharge.milestone.classifyFuelSurchargeSummary

fun summarizeRegionalFuelSurchargeGroupsForOverride(items: MutableList<RegionalFuelSurchargeGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveTieredAuditOverridesFromGroup(items: MutableList<RegionalFuelSurchargeGroup>): MutableList<TieredAuditOverride> {
    val out = mutableListOf<TieredAuditOverride>()
    for (item in items) {
        val derived = newTieredAuditOverride(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeTieredAuditOverride(item: TieredAuditOverride): String {
    return classifyFuelSurchargeSummary(item.measuredValue())
}
