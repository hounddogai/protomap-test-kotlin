package com.acme.transport.safety.coaching.assignment

import com.acme.transport.safety.incident.exception.RegionalIncidentAgreement
import com.acme.transport.safety.incident.exception.classifyRegionalIncidentAgreement

fun summarizeRegionalIncidentAgreementsForTemplate(items: MutableList<RegionalIncidentAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveGlobalCoachingTemplatesFromAgreement(items: MutableList<RegionalIncidentAgreement>): MutableList<GlobalCoachingTemplate> {
    val out = mutableListOf<GlobalCoachingTemplate>()
    for (item in items) {
        val derived = newGlobalCoachingTemplate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreGlobalCoachingTemplate(item: GlobalCoachingTemplate): String {
    return classifyRegionalIncidentAgreement(item.effectiveLoad())
}
