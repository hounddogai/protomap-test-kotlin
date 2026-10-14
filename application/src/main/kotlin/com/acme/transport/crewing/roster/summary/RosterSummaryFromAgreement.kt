package com.acme.transport.crewing.roster.summary

import com.acme.transport.crewing.qualification.digest.CappedQualificationAgreement
import com.acme.transport.crewing.qualification.digest.normalizeQualificationDigestCode

fun summarizeCappedQualificationAgreementsForSummary(items: MutableList<CappedQualificationAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveRosterSummariesFromAgreement(items: MutableList<CappedQualificationAgreement>): MutableList<RosterSummary> {
    val out = mutableListOf<RosterSummary>()
    for (item in items) {
        val derived = newRosterSummary(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planRosterSummary(item: RosterSummary): String = normalizeQualificationDigestCode(item.code)
