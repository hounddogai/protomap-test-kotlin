package com.acme.transport.crewing.roster.mapping

import com.acme.transport.crewing.qualification.audit.QualificationAudit
import com.acme.transport.crewing.qualification.audit.qualificationForecastWeight

fun summarizeQualificationAuditsForMapping(items: MutableList<QualificationAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveDefaultRosterMappingsFromAudit(items: MutableList<QualificationAudit>): MutableList<DefaultRosterMapping> {
    val out = mutableListOf<DefaultRosterMapping>()
    for (item in items) {
        val derived = newDefaultRosterMapping(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeDefaultRosterMapping(item: DefaultRosterMapping): Double = qualificationForecastWeight(3, true)
