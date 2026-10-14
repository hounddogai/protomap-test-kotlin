package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.freightpay.audit.auditoverride.TieredAuditOverride
import com.acme.transport.freightpay.audit.auditoverride.tieredAuditOverrideBucket

fun summarizeTieredAuditOverridesForSchedule(items: MutableList<TieredAuditOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFuelSurchargeSchedulesFromOverride(items: MutableList<TieredAuditOverride>): MutableList<FuelSurchargeSchedule> {
    val out = mutableListOf<FuelSurchargeSchedule>()
    for (item in items) {
        val derived = newFuelSurchargeSchedule(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchFuelSurchargeSchedule(item: FuelSurchargeSchedule): String {
    return tieredAuditOverrideBucket(item.measuredValue())
}
