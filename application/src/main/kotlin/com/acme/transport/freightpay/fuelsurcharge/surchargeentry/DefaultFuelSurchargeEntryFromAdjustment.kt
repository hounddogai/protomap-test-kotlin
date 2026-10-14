package com.acme.transport.freightpay.fuelsurcharge.surchargeentry

import com.acme.transport.freightpay.dispute.schedule.DefaultDisputeAdjustment
import com.acme.transport.freightpay.dispute.schedule.normalizeFallbackDisputeTierCode

fun summarizeDefaultDisputeAdjustmentsForEntry(items: MutableList<DefaultDisputeAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveDefaultFuelSurchargeEntriesFromAdjustment(items: MutableList<DefaultDisputeAdjustment>): MutableList<DefaultFuelSurchargeEntry> {
    val out = mutableListOf<DefaultFuelSurchargeEntry>()
    for (item in items) {
        val derived = newDefaultFuelSurchargeEntry(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceDefaultFuelSurchargeEntry(item: DefaultFuelSurchargeEntry): String {
    return normalizeFallbackDisputeTierCode(item.code)
}
