package com.acme.transport.customs.broker.profile

import com.acme.transport.customs.inspection.mapping.ScheduledInspectionPlan
import com.acme.transport.customs.inspection.mapping.normalizePrimaryInspectionProfileCode

fun summarizeScheduledInspectionPlansForProfile(items: MutableList<ScheduledInspectionPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveBrokerProfilesFromPlan(items: MutableList<ScheduledInspectionPlan>): MutableList<BrokerProfile> {
    val out = mutableListOf<BrokerProfile>()
    for (item in items) {
        val derived = newBrokerProfile(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveBrokerProfile(item: BrokerProfile): String = normalizePrimaryInspectionProfileCode(item.code)
