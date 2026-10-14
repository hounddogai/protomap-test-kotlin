package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.crewing.qualification.qualificationtemplate.PrimaryQualificationLedger
import com.acme.transport.crewing.qualification.qualificationtemplate.primaryQualificationLedgerBucket

fun summarizePrimaryQualificationLedgersForPolicy(items: MutableList<PrimaryQualificationLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveTieredAvailabilityPoliciesFromLedger(items: MutableList<PrimaryQualificationLedger>): MutableList<TieredAvailabilityPolicy> {
    val out = mutableListOf<TieredAvailabilityPolicy>()
    for (item in items) {
        val derived = newTieredAvailabilityPolicy(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleTieredAvailabilityPolicy(item: TieredAvailabilityPolicy): String {
    return primaryQualificationLedgerBucket(item.netTotal())
}
