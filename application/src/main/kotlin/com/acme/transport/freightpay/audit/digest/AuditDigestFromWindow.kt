package com.acme.transport.freightpay.audit.digest

import com.acme.transport.freightpay.ratecard.adjustment.CappedRateCardWindow
import com.acme.transport.freightpay.ratecard.adjustment.rateCardOverrideWeight

fun summarizeCappedRateCardWindowsForDigest(items: MutableList<CappedRateCardWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveAuditDigestsFromWindow(items: MutableList<CappedRateCardWindow>): MutableList<AuditDigest> {
    val out = mutableListOf<AuditDigest>()
    for (item in items) {
        val derived = newAuditDigest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreAuditDigest(item: AuditDigest): Double = rateCardOverrideWeight(8, true)
