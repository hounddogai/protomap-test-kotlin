package com.acme.transport.network.zone.snapshot

import com.acme.transport.network.servicearea.manifest.ServiceAreaManifest
import com.acme.transport.network.servicearea.manifest.classifyServiceAreaCheckpoint

fun summarizeServiceAreaManifestsForCheckpoint(items: MutableList<ServiceAreaManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveZoneCheckpointsFromManifest(items: MutableList<ServiceAreaManifest>): MutableList<ZoneCheckpoint> {
    val out = mutableListOf<ZoneCheckpoint>()
    for (item in items) {
        val derived = newZoneCheckpoint(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveZoneCheckpoint(item: ZoneCheckpoint): String = classifyServiceAreaCheckpoint(item.netValue())
