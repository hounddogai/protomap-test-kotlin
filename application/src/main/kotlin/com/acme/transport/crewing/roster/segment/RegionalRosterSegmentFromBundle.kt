package com.acme.transport.crewing.roster.segment

import com.acme.transport.crewing.qualification.qualificationcycle.PendingQualificationBundle
import com.acme.transport.crewing.qualification.qualificationcycle.legacyQualificationReviewWeight

fun summarizePendingQualificationBundlesForSegment(items: MutableList<PendingQualificationBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRegionalRosterSegmentsFromBundle(items: MutableList<PendingQualificationBundle>): MutableList<RegionalRosterSegment> {
    val out = mutableListOf<RegionalRosterSegment>()
    for (item in items) {
        val derived = newRegionalRosterSegment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeRegionalRosterSegment(item: RegionalRosterSegment): Double = legacyQualificationReviewWeight(2, true)
