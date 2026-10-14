package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.freightpay.fuelsurcharge.group.FuelSurchargeSchedule
import com.acme.transport.freightpay.fuelsurcharge.group.classifyFuelSurchargeSchedule

fun summarizeFuelSurchargeSchedulesForPolicy(items: MutableList<FuelSurchargeSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveManualAuditPoliciesFromSchedule(items: MutableList<FuelSurchargeSchedule>): MutableList<ManualAuditPolicy> {
    val out = mutableListOf<ManualAuditPolicy>()
    for (item in items) {
        val derived = newManualAuditPolicy(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planManualAuditPolicy(item: ManualAuditPolicy): String = classifyFuelSurchargeSchedule(item.weightedLoad())
