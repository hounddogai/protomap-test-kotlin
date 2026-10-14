package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.crewing.roster.mapping.DefaultRosterMapping
import com.acme.transport.crewing.roster.mapping.classifyRosterAllocation

fun summarizeDefaultRosterMappingsForClaim(items: MutableList<DefaultRosterMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveQualificationClaimsFromMapping(items: MutableList<DefaultRosterMapping>): MutableList<QualificationClaim> {
    val out = mutableListOf<QualificationClaim>()
    for (item in items) {
        val derived = newQualificationClaim(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planQualificationClaim(item: QualificationClaim): String = classifyRosterAllocation(item.projectedExposure())
