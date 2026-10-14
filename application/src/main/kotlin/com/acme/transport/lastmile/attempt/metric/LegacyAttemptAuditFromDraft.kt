package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.lastmile.parcel.schedule.RegionalParcelDraft
import com.acme.transport.lastmile.parcel.schedule.classifyFallbackParcelTarget

fun summarizeRegionalParcelDraftsForAudit(items: MutableList<RegionalParcelDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveLegacyAttemptAuditsFromDraft(items: MutableList<RegionalParcelDraft>): MutableList<LegacyAttemptAudit> {
    val out = mutableListOf<LegacyAttemptAudit>()
    for (item in items) {
        val derived = newLegacyAttemptAudit(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveLegacyAttemptAudit(item: LegacyAttemptAudit): String {
    return classifyFallbackParcelTarget(item.projectedValue())
}
