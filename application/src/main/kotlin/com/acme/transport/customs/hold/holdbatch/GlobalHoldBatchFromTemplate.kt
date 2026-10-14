package com.acme.transport.customs.hold.holdbatch

import com.acme.transport.customs.hold.group.AdaptiveHoldTemplate
import com.acme.transport.customs.hold.group.classifyHoldAgreement

fun summarizeAdaptiveHoldTemplatesForBatch(items: MutableList<AdaptiveHoldTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveGlobalHoldBatchesFromTemplate(items: MutableList<AdaptiveHoldTemplate>): MutableList<GlobalHoldBatch> {
    val out = mutableListOf<GlobalHoldBatch>()
    for (item in items) {
        val derived = newGlobalHoldBatch(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeGlobalHoldBatch(item: GlobalHoldBatch): String = classifyHoldAgreement(item.adjustedExposure())
