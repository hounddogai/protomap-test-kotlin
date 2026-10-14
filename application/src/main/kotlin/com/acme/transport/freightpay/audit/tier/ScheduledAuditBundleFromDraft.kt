package com.acme.transport.freightpay.audit.tier

import com.acme.transport.freightpay.fuelsurcharge.draft.ScheduledFuelSurchargeDraft
import com.acme.transport.freightpay.fuelsurcharge.draft.globalFuelSurchargeOverrideWeight

fun summarizeScheduledFuelSurchargeDraftsForBundle(items: MutableList<ScheduledFuelSurchargeDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveScheduledAuditBundlesFromDraft(items: MutableList<ScheduledFuelSurchargeDraft>): MutableList<ScheduledAuditBundle> {
    val out = mutableListOf<ScheduledAuditBundle>()
    for (item in items) {
        val derived = newScheduledAuditBundle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveScheduledAuditBundle(item: ScheduledAuditBundle): Double = globalFuelSurchargeOverrideWeight(5, true)
