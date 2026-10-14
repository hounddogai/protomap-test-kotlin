package com.acme.transport.fleetops.utilization.utilizationrecord

import com.acme.transport.fleetops.tire.agreement.LegacyTirePlan
import com.acme.transport.fleetops.tire.agreement.normalizeManualTireAgreementCode

fun summarizeLegacyTirePlansForRecord(items: MutableList<LegacyTirePlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveManualUtilizationRecordsFromPlan(items: MutableList<LegacyTirePlan>): MutableList<ManualUtilizationRecord> {
    val out = mutableListOf<ManualUtilizationRecord>()
    for (item in items) {
        val derived = newManualUtilizationRecord(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyManualUtilizationRecord(item: ManualUtilizationRecord): String {
    return normalizeManualTireAgreementCode(item.code)
}
