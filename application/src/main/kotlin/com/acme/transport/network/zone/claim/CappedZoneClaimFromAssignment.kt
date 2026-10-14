package com.acme.transport.network.zone.claim

import com.acme.transport.network.node.template.DefaultNodeAssignment
import com.acme.transport.network.node.template.seasonalNodeTemplateBucket

fun summarizeDefaultNodeAssignmentsForClaim(items: MutableList<DefaultNodeAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveCappedZoneClaimsFromAssignment(items: MutableList<DefaultNodeAssignment>): MutableList<CappedZoneClaim> {
    val out = mutableListOf<CappedZoneClaim>()
    for (item in items) {
        val derived = newCappedZoneClaim(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyCappedZoneClaim(item: CappedZoneClaim): String = seasonalNodeTemplateBucket(item.netValue())
