package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import com.acme.transport.linehaul.tender.template.PrimaryTenderException
import com.acme.transport.linehaul.tender.template.primaryTenderExceptionBucket

fun summarizePrimaryTenderExceptionsForScore(items: MutableList<PrimaryTenderException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun derivePendingBackhaulScoresFromException(items: MutableList<PrimaryTenderException>): MutableList<PendingBackhaulScore> {
    val out = mutableListOf<PendingBackhaulScore>()
    for (item in items) {
        val derived = newPendingBackhaulScore(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun derivePendingBackhaulScore(item: PendingBackhaulScore): String {
    return primaryTenderExceptionBucket(item.adjustedExposure())
}
