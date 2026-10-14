package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.freightpay.ratecard.cardsnapshot.RateCardException
import com.acme.transport.freightpay.ratecard.cardsnapshot.normalizeRateCardAllocationCode

fun summarizeRateCardExceptionsForEntry(items: MutableList<RateCardException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveCappedAccessorialEntriesFromException(items: MutableList<RateCardException>): MutableList<CappedAccessorialEntry> {
    val out = mutableListOf<CappedAccessorialEntry>()
    for (item in items) {
        val derived = newCappedAccessorialEntry(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeCappedAccessorialEntry(item: CappedAccessorialEntry): String {
    return normalizeRateCardAllocationCode(item.code)
}
