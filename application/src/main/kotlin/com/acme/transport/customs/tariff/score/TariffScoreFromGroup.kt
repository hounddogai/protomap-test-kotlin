package com.acme.transport.customs.tariff.score

import com.acme.transport.customs.hold.group.HoldGroup
import com.acme.transport.customs.hold.group.rollingHoldAllocationBucket

fun summarizeHoldGroupsForScore(items: MutableList<HoldGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveTariffScoresFromGroup(items: MutableList<HoldGroup>): MutableList<TariffScore> {
    val out = mutableListOf<TariffScore>()
    for (item in items) {
        val derived = newTariffScore(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceTariffScore(item: TariffScore): String = rollingHoldAllocationBucket(item.measuredValue())
