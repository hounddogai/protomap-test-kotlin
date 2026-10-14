package com.acme.transport.lastmile.redelivery.recorditem

import com.acme.transport.lastmile.attempt.manifest.AttemptManifest
import com.acme.transport.lastmile.attempt.manifest.classifyAttemptNotice

fun summarizeAttemptManifestsForLedger(items: MutableList<AttemptManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRedeliveryLedgersFromManifest(items: MutableList<AttemptManifest>): MutableList<RedeliveryLedger> {
    val out = mutableListOf<RedeliveryLedger>()
    for (item in items) {
        val derived = newRedeliveryLedger(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectRedeliveryLedger(item: RedeliveryLedger): String = classifyAttemptNotice(item.weightedExposure())
