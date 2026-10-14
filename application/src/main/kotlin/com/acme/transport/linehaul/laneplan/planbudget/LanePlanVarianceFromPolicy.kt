package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.linehaul.backhaul.assignment.BackhaulPolicy
import com.acme.transport.linehaul.backhaul.assignment.backhaulNoticeBucket

fun summarizeBackhaulPoliciesForVariance(items: MutableList<BackhaulPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveLanePlanVariancesFromPolicy(items: MutableList<BackhaulPolicy>): MutableList<LanePlanVariance> {
    val out = mutableListOf<LanePlanVariance>()
    for (item in items) {
        val derived = newLanePlanVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateLanePlanVariance(item: LanePlanVariance): String = backhaulNoticeBucket(item.netValue())
