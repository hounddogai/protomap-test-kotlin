package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.linehaul.relay.relayassignment.RollingRelayRecord
import com.acme.transport.linehaul.relay.relayassignment.normalizeManualRelayReservationCode

fun summarizeRollingRelayRecordsForMetric(items: MutableList<RollingRelayRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveDefaultTrailerMetricsFromRecord(items: MutableList<RollingRelayRecord>): MutableList<DefaultTrailerMetric> {
    val out = mutableListOf<DefaultTrailerMetric>()
    for (item in items) {
        val derived = newDefaultTrailerMetric(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceDefaultTrailerMetric(item: DefaultTrailerMetric): String {
    return normalizeManualRelayReservationCode(item.code)
}
