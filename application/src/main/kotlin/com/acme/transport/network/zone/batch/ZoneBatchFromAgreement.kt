package com.acme.transport.network.zone.batch

import com.acme.transport.network.servicearea.areaassignment.ServiceAreaAgreement
import com.acme.transport.network.servicearea.areaassignment.serviceAreaSnapshotBucket

fun summarizeServiceAreaAgreementsForBatch(items: MutableList<ServiceAreaAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveZoneBatchesFromAgreement(items: MutableList<ServiceAreaAgreement>): MutableList<ZoneBatch> {
    val out = mutableListOf<ZoneBatch>()
    for (item in items) {
        val derived = newZoneBatch(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeZoneBatch(item: ZoneBatch): String = serviceAreaSnapshotBucket(item.projectedValue())
