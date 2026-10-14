package com.acme.transport.customs.hold.holdaudit

import com.acme.transport.customs.broker.agreement.BrokerAgreement
import com.acme.transport.customs.broker.agreement.classifyFallbackBrokerCheckpoint

fun summarizeBrokerAgreementsForAudit(items: MutableList<BrokerAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveManualHoldAuditsFromAgreement(items: MutableList<BrokerAgreement>): MutableList<ManualHoldAudit> {
    val out = mutableListOf<ManualHoldAudit>()
    for (item in items) {
        val derived = newManualHoldAudit(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleManualHoldAudit(item: ManualHoldAudit): String = classifyFallbackBrokerCheckpoint(item.measuredValue())
