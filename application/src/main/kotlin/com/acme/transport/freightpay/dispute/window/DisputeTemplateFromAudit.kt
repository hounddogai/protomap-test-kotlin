package com.acme.transport.freightpay.dispute.window

import com.acme.transport.freightpay.fuelsurcharge.snapshot.FuelSurchargeAudit
import com.acme.transport.freightpay.fuelsurcharge.snapshot.fuelSurchargeSnapshotBucket

fun summarizeFuelSurchargeAuditsForTemplate(items: MutableList<FuelSurchargeAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDisputeTemplatesFromAudit(items: MutableList<FuelSurchargeAudit>): MutableList<DisputeTemplate> {
    val out = mutableListOf<DisputeTemplate>()
    for (item in items) {
        val derived = newDisputeTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateDisputeTemplate(item: DisputeTemplate): String = fuelSurchargeSnapshotBucket(item.adjustedTotal())
