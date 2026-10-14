package com.acme.transport.customs.hold.holdaudit

import com.acme.transport.customs.tariff.fallbacktariffcycle.TariffSummary
import com.acme.transport.customs.tariff.fallbacktariffcycle.tariffSummaryBucket

fun summarizeTariffSummariesForAudit(items: MutableList<TariffSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveManualHoldAuditsFromSummary(items: MutableList<TariffSummary>): MutableList<ManualHoldAudit> {
    val out = mutableListOf<ManualHoldAudit>()
    for (item in items) {
        val derived = newManualHoldAudit(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planManualHoldAudit(item: ManualHoldAudit): String = tariffSummaryBucket(item.measuredValue())
