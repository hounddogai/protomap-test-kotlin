package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.freightpay.ratecard.variance.ManualRateCardThreshold
import com.acme.transport.freightpay.ratecard.variance.classifyStrictRateCardException

fun summarizeManualRateCardThresholdsForTarget(items: MutableList<ManualRateCardThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveCappedAuditTargetsFromThreshold(items: MutableList<ManualRateCardThreshold>): MutableList<CappedAuditTarget> {
    val out = mutableListOf<CappedAuditTarget>()
    for (item in items) {
        val derived = newCappedAuditTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocateCappedAuditTarget(item: CappedAuditTarget): String {
    return classifyStrictRateCardException(item.measuredValue())
}
