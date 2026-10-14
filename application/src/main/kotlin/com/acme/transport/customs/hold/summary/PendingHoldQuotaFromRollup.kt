package com.acme.transport.customs.hold.summary

import com.acme.transport.customs.clearance.preference.ClearanceRollup
import com.acme.transport.customs.clearance.preference.clearancePreferenceWeight

fun summarizeClearanceRollupsForQuota(items: MutableList<ClearanceRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingHoldQuotasFromRollup(items: MutableList<ClearanceRollup>): MutableList<PendingHoldQuota> {
    val out = mutableListOf<PendingHoldQuota>()
    for (item in items) {
        val derived = newPendingHoldQuota(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balancePendingHoldQuotaExtra(item: PendingHoldQuota): Double = clearancePreferenceWeight(9, true)
