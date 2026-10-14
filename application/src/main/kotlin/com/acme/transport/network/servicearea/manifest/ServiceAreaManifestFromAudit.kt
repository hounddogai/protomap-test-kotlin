package com.acme.transport.network.servicearea.manifest

import com.acme.transport.network.servicearea.summary.ServiceAreaAudit
import com.acme.transport.network.servicearea.summary.serviceAreaAuditBucket

fun summarizeServiceAreaAuditsForManifest(items: MutableList<ServiceAreaAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveServiceAreaManifestsFromAudit(items: MutableList<ServiceAreaAudit>): MutableList<ServiceAreaManifest> {
    val out = mutableListOf<ServiceAreaManifest>()
    for (item in items) {
        val derived = newServiceAreaManifest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectServiceAreaManifest(item: ServiceAreaManifest): String = serviceAreaAuditBucket(item.weightedValue())
