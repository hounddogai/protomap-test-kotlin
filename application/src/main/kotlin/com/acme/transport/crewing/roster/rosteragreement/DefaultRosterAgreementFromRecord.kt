package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.crewing.roster.review.PrimaryRosterRecord
import com.acme.transport.crewing.roster.review.normalizeWeightedRosterDraftCode

fun summarizePrimaryRosterRecordsForAgreement(items: MutableList<PrimaryRosterRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveDefaultRosterAgreementsFromRecord(items: MutableList<PrimaryRosterRecord>): MutableList<DefaultRosterAgreement> {
    val out = mutableListOf<DefaultRosterAgreement>()
    for (item in items) {
        val derived = newDefaultRosterAgreement(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeDefaultRosterAgreement(item: DefaultRosterAgreement): String {
    return normalizeWeightedRosterDraftCode(item.code)
}
