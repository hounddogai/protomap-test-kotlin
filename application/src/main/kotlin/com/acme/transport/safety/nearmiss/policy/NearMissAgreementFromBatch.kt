package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.safety.incident.batch.GlobalIncidentBatch
import com.acme.transport.safety.incident.batch.normalizeGlobalIncidentBatchCode

fun summarizeGlobalIncidentBatchesForAgreement(items: MutableList<GlobalIncidentBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveNearMissAgreementsFromBatch(items: MutableList<GlobalIncidentBatch>): MutableList<NearMissAgreement> {
    val out = mutableListOf<NearMissAgreement>()
    for (item in items) {
        val derived = newNearMissAgreement(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveNearMissAgreement(item: NearMissAgreement): String = normalizeGlobalIncidentBatchCode(item.code)
