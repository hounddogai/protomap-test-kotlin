package com.acme.transport.safety.violation.quota

import com.acme.transport.safety.incident.reservation.IncidentPreference
import com.acme.transport.safety.incident.reservation.normalizeIncidentReservationCode

fun summarizeIncidentPreferencesForQuota(items: MutableList<IncidentPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveViolationQuotasFromPreference(items: MutableList<IncidentPreference>): MutableList<ViolationQuota> {
    val out = mutableListOf<ViolationQuota>()
    for (item in items) {
        val derived = newViolationQuota(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyViolationQuota(item: ViolationQuota): String = normalizeIncidentReservationCode(item.code)
