package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.fleetops.defect.template.AdaptiveDefectAudit
import com.acme.transport.fleetops.defect.template.classifyAdaptiveDefectTemplate

fun summarizeAdaptiveDefectAuditsForAgreement(items: MutableList<AdaptiveDefectAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveManualTireAgreementsFromAudit(items: MutableList<AdaptiveDefectAudit>): MutableList<ManualTireAgreement> {
    val out = mutableListOf<ManualTireAgreement>()
    for (item in items) {
        val derived = newManualTireAgreement(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileManualTireAgreement(item: ManualTireAgreement): String {
    return classifyAdaptiveDefectTemplate(item.effectiveLoad())
}
