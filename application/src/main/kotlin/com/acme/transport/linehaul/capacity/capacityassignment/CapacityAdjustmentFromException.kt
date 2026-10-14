package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.linehaul.tender.template.PrimaryTenderException
import com.acme.transport.linehaul.tender.template.primaryTenderPreferenceBucket

fun summarizePrimaryTenderExceptionsForAdjustment(items: MutableList<PrimaryTenderException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveCapacityAdjustmentsFromException(items: MutableList<PrimaryTenderException>): MutableList<CapacityAdjustment> {
    val out = mutableListOf<CapacityAdjustment>()
    for (item in items) {
        val derived = newCapacityAdjustment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveCapacityAdjustment(item: CapacityAdjustment): String {
    return primaryTenderPreferenceBucket(item.adjustedLoad())
}
