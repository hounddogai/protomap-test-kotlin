package com.acme.transport.safety.scorecard.slot

import com.acme.transport.safety.violation.violationquota.ViolationNotice
import com.acme.transport.safety.violation.violationquota.violationNoticeWeight

fun summarizeViolationNoticesForNotice(items: MutableList<ViolationNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveTieredScorecardNoticesFromNotice(items: MutableList<ViolationNotice>): MutableList<TieredScorecardNotice> {
    val out = mutableListOf<TieredScorecardNotice>()
    for (item in items) {
        val derived = newTieredScorecardNotice(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreTieredScorecardNotice(item: TieredScorecardNotice): Double = violationNoticeWeight(4, true)
