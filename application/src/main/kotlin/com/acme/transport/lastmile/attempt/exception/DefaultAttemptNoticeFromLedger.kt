package com.acme.transport.lastmile.attempt.exception

import com.acme.transport.lastmile.drop.forecast.DropLedger
import com.acme.transport.lastmile.drop.forecast.classifyTieredDropTemplate

fun summarizeDropLedgersForNotice(items: MutableList<DropLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveDefaultAttemptNoticesFromLedger(items: MutableList<DropLedger>): MutableList<DefaultAttemptNotice> {
    val out = mutableListOf<DefaultAttemptNotice>()
    for (item in items) {
        val derived = newDefaultAttemptNotice(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitDefaultAttemptNotice(item: DefaultAttemptNotice): String {
    return classifyTieredDropTemplate(item.adjustedLoad())
}
