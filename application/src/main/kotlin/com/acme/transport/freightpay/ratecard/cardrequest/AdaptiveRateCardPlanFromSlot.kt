package com.acme.transport.freightpay.ratecard.cardrequest

import com.acme.transport.freightpay.audit.slot.ManualAuditSlot
import com.acme.transport.freightpay.audit.slot.legacyAuditManifestWeight

fun summarizeManualAuditSlotsForPlan(items: MutableList<ManualAuditSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveAdaptiveRateCardPlansFromSlot(items: MutableList<ManualAuditSlot>): MutableList<AdaptiveRateCardPlan> {
    val out = mutableListOf<AdaptiveRateCardPlan>()
    for (item in items) {
        val derived = newAdaptiveRateCardPlan(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectAdaptiveRateCardPlan(item: AdaptiveRateCardPlan): Double = legacyAuditManifestWeight(8, true)
