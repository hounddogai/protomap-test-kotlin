package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import com.acme.transport.freightpay.audit.draft.TieredAuditDigest
import com.acme.transport.freightpay.audit.draft.classifySeasonalAuditDraft

fun summarizeTieredAuditDigestsForCycle(items: MutableList<TieredAuditDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScheduledFuelSurchargeCyclesFromDigest(items: MutableList<TieredAuditDigest>): MutableList<ScheduledFuelSurchargeCycle> {
    val out = mutableListOf<ScheduledFuelSurchargeCycle>()
    for (item in items) {
        val derived = newScheduledFuelSurchargeCycle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitScheduledFuelSurchargeCycle(item: ScheduledFuelSurchargeCycle): String {
    return classifySeasonalAuditDraft(item.measuredValue())
}
