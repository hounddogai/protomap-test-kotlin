package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.fleetops.leasereturn.returnschedule.BatchedLeaseReturnRule
import com.acme.transport.fleetops.leasereturn.returnschedule.batchedLeaseReturnRuleWeight

fun summarizeBatchedLeaseReturnRulesForManifest(items: MutableList<BatchedLeaseReturnRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveLeaseReturnManifestsFromRule(items: MutableList<BatchedLeaseReturnRule>): MutableList<LeaseReturnManifest> {
    val out = mutableListOf<LeaseReturnManifest>()
    for (item in items) {
        val derived = newLeaseReturnManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planLeaseReturnManifest(item: LeaseReturnManifest): Double = batchedLeaseReturnRuleWeight(9, true)
