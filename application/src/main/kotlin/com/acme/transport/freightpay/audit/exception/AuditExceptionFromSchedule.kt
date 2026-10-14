package com.acme.transport.freightpay.audit.exception

import com.acme.transport.freightpay.dispute.schedule.DisputeSchedule
import com.acme.transport.freightpay.dispute.schedule.classifyFallbackDisputeTier

fun summarizeDisputeSchedulesForException(items: MutableList<DisputeSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveAuditExceptionsFromSchedule(items: MutableList<DisputeSchedule>): MutableList<AuditException> {
    val out = mutableListOf<AuditException>()
    for (item in items) {
        val derived = newAuditException(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveAuditException(item: AuditException): String = classifyFallbackDisputeTier(item.projectedTotal())
