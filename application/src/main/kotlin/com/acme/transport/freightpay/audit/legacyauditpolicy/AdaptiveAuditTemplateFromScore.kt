package com.acme.transport.freightpay.audit.legacyauditpolicy

import com.acme.transport.freightpay.accessorial.accessorialmetric.SeasonalAccessorialScore
import com.acme.transport.freightpay.accessorial.accessorialmetric.adaptiveAccessorialRequestWeight

fun summarizeSeasonalAccessorialScoresForTemplate(items: MutableList<SeasonalAccessorialScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveAdaptiveAuditTemplatesFromScore(items: MutableList<SeasonalAccessorialScore>): MutableList<AdaptiveAuditTemplate> {
    val out = mutableListOf<AdaptiveAuditTemplate>()
    for (item in items) {
        val derived = newAdaptiveAuditTemplate(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeAdaptiveAuditTemplate(item: AdaptiveAuditTemplate): Double {
    return adaptiveAccessorialRequestWeight(1, true)
}
