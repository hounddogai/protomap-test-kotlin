package com.acme.transport.lastmile.drop.digest

import com.acme.transport.lastmile.redelivery.digest.RedeliveryDigest
import com.acme.transport.lastmile.redelivery.digest.redeliverySlotWeight

fun summarizeRedeliveryDigestsForDigest(items: MutableList<RedeliveryDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveDropDigestsFromDigest(items: MutableList<RedeliveryDigest>): MutableList<DropDigest> {
    val out = mutableListOf<DropDigest>()
    for (item in items) {
        val derived = newDropDigest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDropDigest(item: DropDigest): Double = redeliverySlotWeight(3, true)
