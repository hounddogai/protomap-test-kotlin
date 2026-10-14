package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.safety.scorecard.scorecardproposal.RegionalScorecardAudit
import com.acme.transport.safety.scorecard.scorecardproposal.normalizeDefaultScorecardReviewCode

fun summarizeRegionalScorecardAuditsForTarget(items: MutableList<RegionalScorecardAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun derivePrimaryCameraEventTargetsFromAudit(items: MutableList<RegionalScorecardAudit>): MutableList<PrimaryCameraEventTarget> {
    val out = mutableListOf<PrimaryCameraEventTarget>()
    for (item in items) {
        val derived = newPrimaryCameraEventTarget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimatePrimaryCameraEventTarget(item: PrimaryCameraEventTarget): String {
    return normalizeDefaultScorecardReviewCode(item.code)
}
