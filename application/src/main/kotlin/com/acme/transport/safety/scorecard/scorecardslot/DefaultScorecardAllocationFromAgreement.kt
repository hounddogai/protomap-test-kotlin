package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.safety.incident.claim.IncidentAgreement
import com.acme.transport.safety.incident.claim.normalizeIncidentScheduleCode

fun summarizeIncidentAgreementsForAllocation(items: MutableList<IncidentAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveDefaultScorecardAllocationsFromAgreement(items: MutableList<IncidentAgreement>): MutableList<DefaultScorecardAllocation> {
    val out = mutableListOf<DefaultScorecardAllocation>()
    for (item in items) {
        val derived = newDefaultScorecardAllocation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planDefaultScorecardAllocation(item: DefaultScorecardAllocation): String {
    return normalizeIncidentScheduleCode(item.code)
}
