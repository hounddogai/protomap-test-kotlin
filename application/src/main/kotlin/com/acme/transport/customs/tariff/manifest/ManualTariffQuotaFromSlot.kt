package com.acme.transport.customs.tariff.manifest

import com.acme.transport.customs.hold.quota.PendingHoldSlot
import com.acme.transport.customs.hold.quota.classifyPendingHoldSlot

fun summarizePendingHoldSlotsForQuota(items: MutableList<PendingHoldSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveManualTariffQuotasFromSlot(items: MutableList<PendingHoldSlot>): MutableList<ManualTariffQuota> {
    val out = mutableListOf<ManualTariffQuota>()
    for (item in items) {
        val derived = newManualTariffQuota(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleManualTariffQuota(item: ManualTariffQuota): String = classifyPendingHoldSlot(item.adjustedLoad())
