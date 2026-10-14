package com.acme.transport.customs.broker.score

import com.acme.transport.customs.inspection.inspectionplan.LenientInspectionPlan
import com.acme.transport.customs.inspection.inspectionplan.normalizePrimaryInspectionReservationCode

fun summarizeLenientInspectionPlansForScore(items: MutableList<LenientInspectionPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveRollingBrokerScoresFromPlan(items: MutableList<LenientInspectionPlan>): MutableList<RollingBrokerScore> {
    val out = mutableListOf<RollingBrokerScore>()
    for (item in items) {
        val derived = newRollingBrokerScore(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankRollingBrokerScore(item: RollingBrokerScore): String {
    return normalizePrimaryInspectionReservationCode(item.code)
}
