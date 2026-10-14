package com.acme.transport.linehaul.trailer.request

import com.acme.transport.linehaul.backhaul.backhaulbatch.BackhaulRequest
import com.acme.transport.linehaul.backhaul.backhaulbatch.manualBackhaulBatchWeight

fun summarizeBackhaulRequestsForException(items: MutableList<BackhaulRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveDefaultTrailerExceptionsFromRequest(items: MutableList<BackhaulRequest>): MutableList<DefaultTrailerException> {
    val out = mutableListOf<DefaultTrailerException>()
    for (item in items) {
        val derived = newDefaultTrailerException(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitDefaultTrailerException(item: DefaultTrailerException): Double = manualBackhaulBatchWeight(9, true)
