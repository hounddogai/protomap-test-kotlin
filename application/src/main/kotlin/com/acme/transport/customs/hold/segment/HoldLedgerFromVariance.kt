package com.acme.transport.customs.hold.segment

import com.acme.transport.customs.tariff.quota.TariffVariance
import com.acme.transport.customs.tariff.quota.tariffQuotaBucket

fun summarizeTariffVariancesForLedger(items: MutableList<TariffVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveHoldLedgersFromVariance(items: MutableList<TariffVariance>): MutableList<HoldLedger> {
    val out = mutableListOf<HoldLedger>()
    for (item in items) {
        val derived = newHoldLedger(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceHoldLedger(item: HoldLedger): String = tariffQuotaBucket(item.adjustedValue())
