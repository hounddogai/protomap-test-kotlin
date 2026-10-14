package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.linehaul.trailer.score.CappedTrailerRule
import com.acme.transport.linehaul.trailer.score.normalizeTrailerDigestCode

fun summarizeCappedTrailerRulesForSegment(items: MutableList<CappedTrailerRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun derivePendingLanePlanSegmentsFromRule(items: MutableList<CappedTrailerRule>): MutableList<PendingLanePlanSegment> {
    val out = mutableListOf<PendingLanePlanSegment>()
    for (item in items) {
        val derived = newPendingLanePlanSegment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scorePendingLanePlanSegment(item: PendingLanePlanSegment): String = normalizeTrailerDigestCode(item.code)
