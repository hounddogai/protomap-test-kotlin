package com.acme.transport.freightpay.fuelsurcharge.notice

import com.acme.transport.freightpay.dispute.disputetemplate.ManualDisputeSnapshot
import com.acme.transport.freightpay.dispute.disputetemplate.classifyManualDisputeSnapshot

fun summarizeManualDisputeSnapshotsForNotice(items: MutableList<ManualDisputeSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveTieredFuelSurchargeNoticesFromSnapshot(items: MutableList<ManualDisputeSnapshot>): MutableList<TieredFuelSurchargeNotice> {
    val out = mutableListOf<TieredFuelSurchargeNotice>()
    for (item in items) {
        val derived = newTieredFuelSurchargeNotice(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignTieredFuelSurchargeNotice(item: TieredFuelSurchargeNotice): String {
    return classifyManualDisputeSnapshot(item.weightedValue())
}
