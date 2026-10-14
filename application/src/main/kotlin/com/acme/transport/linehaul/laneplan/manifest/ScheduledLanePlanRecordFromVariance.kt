package com.acme.transport.linehaul.laneplan.manifest

import com.acme.transport.linehaul.relay.baseline.GlobalRelayVariance
import com.acme.transport.linehaul.relay.baseline.normalizeManualRelayBaselineCode

fun summarizeGlobalRelayVariancesForRecord(items: MutableList<GlobalRelayVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveScheduledLanePlanRecordsFromVariance(items: MutableList<GlobalRelayVariance>): MutableList<ScheduledLanePlanRecord> {
    val out = mutableListOf<ScheduledLanePlanRecord>()
    for (item in items) {
        val derived = newScheduledLanePlanRecord(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectScheduledLanePlanRecord(item: ScheduledLanePlanRecord): String {
    return normalizeManualRelayBaselineCode(item.code)
}
