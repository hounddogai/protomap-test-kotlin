package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.freightpay.audit.manifest.AuditEstimate
import com.acme.transport.freightpay.audit.manifest.regionalAuditWindowBucket

fun summarizeAuditEstimatesForPreference(items: MutableList<AuditEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveRollingFuelSurchargePreferencesFromEstimate(items: MutableList<AuditEstimate>): MutableList<RollingFuelSurchargePreference> {
    val out = mutableListOf<RollingFuelSurchargePreference>()
    for (item in items) {
        val derived = newRollingFuelSurchargePreference(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeRollingFuelSurchargePreference(item: RollingFuelSurchargePreference): String {
    return regionalAuditWindowBucket(item.adjustedVolume())
}
