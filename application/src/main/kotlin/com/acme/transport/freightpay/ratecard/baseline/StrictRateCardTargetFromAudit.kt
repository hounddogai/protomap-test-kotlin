package com.acme.transport.freightpay.ratecard.baseline

import com.acme.transport.freightpay.accessorial.audit.CappedAccessorialAudit
import com.acme.transport.freightpay.accessorial.audit.classifyDefaultAccessorialRollup

fun summarizeCappedAccessorialAuditsForTarget(items: MutableList<CappedAccessorialAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveStrictRateCardTargetsFromAudit(items: MutableList<CappedAccessorialAudit>): MutableList<StrictRateCardTarget> {
    val out = mutableListOf<StrictRateCardTarget>()
    for (item in items) {
        val derived = newStrictRateCardTarget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleStrictRateCardTarget(item: StrictRateCardTarget): String {
    return classifyDefaultAccessorialRollup(item.effectiveValue())
}
