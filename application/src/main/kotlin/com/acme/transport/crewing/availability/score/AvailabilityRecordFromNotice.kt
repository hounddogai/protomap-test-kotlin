package com.acme.transport.crewing.availability.score

import com.acme.transport.crewing.qualification.proposal.RollingQualificationNotice
import com.acme.transport.crewing.qualification.proposal.normalizeRegionalQualificationProposalCode

fun summarizeRollingQualificationNoticesForRecord(items: MutableList<RollingQualificationNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveAvailabilityRecordsFromNotice(items: MutableList<RollingQualificationNotice>): MutableList<AvailabilityRecord> {
    val out = mutableListOf<AvailabilityRecord>()
    for (item in items) {
        val derived = newAvailabilityRecord(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeAvailabilityRecord(item: AvailabilityRecord): String {
    return normalizeRegionalQualificationProposalCode(item.code)
}
