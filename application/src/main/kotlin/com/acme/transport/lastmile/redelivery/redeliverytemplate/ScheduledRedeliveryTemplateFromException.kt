package com.acme.transport.lastmile.redelivery.redeliverytemplate

import com.acme.transport.lastmile.attempt.adaptiveattemptrequest.FallbackAttemptException
import com.acme.transport.lastmile.attempt.adaptiveattemptrequest.adaptiveAttemptRequestBucket

fun summarizeFallbackAttemptExceptionsForTemplate(items: MutableList<FallbackAttemptException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveScheduledRedeliveryTemplatesFromException(items: MutableList<FallbackAttemptException>): MutableList<ScheduledRedeliveryTemplate> {
    val out = mutableListOf<ScheduledRedeliveryTemplate>()
    for (item in items) {
        val derived = newScheduledRedeliveryTemplate(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planScheduledRedeliveryTemplate(item: ScheduledRedeliveryTemplate): String {
    return adaptiveAttemptRequestBucket(item.netValue())
}
