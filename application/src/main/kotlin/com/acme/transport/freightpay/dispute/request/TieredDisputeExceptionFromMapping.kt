package com.acme.transport.freightpay.dispute.request

import com.acme.transport.freightpay.audit.slot.RollingAuditMapping
import com.acme.transport.freightpay.audit.slot.legacyAuditManifestWeight

fun summarizeRollingAuditMappingsForException(items: MutableList<RollingAuditMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTieredDisputeExceptionsFromMapping(items: MutableList<RollingAuditMapping>): MutableList<TieredDisputeException> {
    val out = mutableListOf<TieredDisputeException>()
    for (item in items) {
        val derived = newTieredDisputeException(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeTieredDisputeException(item: TieredDisputeException): Double = legacyAuditManifestWeight(6, true)
