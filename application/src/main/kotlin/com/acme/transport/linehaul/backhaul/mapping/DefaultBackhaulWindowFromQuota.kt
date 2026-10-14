package com.acme.transport.linehaul.backhaul.mapping

import com.acme.transport.linehaul.laneplan.quota.ScheduledLanePlanQuota
import com.acme.transport.linehaul.laneplan.quota.normalizeRollingLanePlanLedgerCode

fun summarizeScheduledLanePlanQuotasForWindow(items: MutableList<ScheduledLanePlanQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultBackhaulWindowsFromQuota(items: MutableList<ScheduledLanePlanQuota>): MutableList<DefaultBackhaulWindow> {
    val out = mutableListOf<DefaultBackhaulWindow>()
    for (item in items) {
        val derived = newDefaultBackhaulWindow(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectDefaultBackhaulWindow(item: DefaultBackhaulWindow): String {
    return normalizeRollingLanePlanLedgerCode(item.code)
}
