package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.lastmile.attempt.template.CappedAttemptException
import com.acme.transport.lastmile.attempt.template.cappedAttemptExceptionWeight

fun summarizeCappedAttemptExceptionsForBundle(items: MutableList<CappedAttemptException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveFallbackRedeliveryBundlesFromException(items: MutableList<CappedAttemptException>): MutableList<FallbackRedeliveryBundle> {
    val out = mutableListOf<FallbackRedeliveryBundle>()
    for (item in items) {
        val derived = newFallbackRedeliveryBundle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeFallbackRedeliveryBundle(item: FallbackRedeliveryBundle): Double {
    return cappedAttemptExceptionWeight(8, true)
}
