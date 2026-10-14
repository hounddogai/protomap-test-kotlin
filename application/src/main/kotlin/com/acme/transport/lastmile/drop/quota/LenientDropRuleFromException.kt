package com.acme.transport.lastmile.drop.quota

import com.acme.transport.lastmile.redelivery.exception.RedeliveryException
import com.acme.transport.lastmile.redelivery.exception.redeliveryExceptionWeight

fun summarizeRedeliveryExceptionsForRule(items: MutableList<RedeliveryException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveLenientDropRulesFromException(items: MutableList<RedeliveryException>): MutableList<LenientDropRule> {
    val out = mutableListOf<LenientDropRule>()
    for (item in items) {
        val derived = newLenientDropRule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeLenientDropRule(item: LenientDropRule): Double = redeliveryExceptionWeight(7, true)
