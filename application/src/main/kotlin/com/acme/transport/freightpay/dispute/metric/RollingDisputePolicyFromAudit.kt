package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.freightpay.ratecard.threshold.DefaultRateCardAudit
import com.acme.transport.freightpay.ratecard.threshold.strictRateCardThresholdBucket

fun summarizeDefaultRateCardAuditsForPolicy(items: MutableList<DefaultRateCardAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveRollingDisputePoliciesFromAudit(items: MutableList<DefaultRateCardAudit>): MutableList<RollingDisputePolicy> {
    val out = mutableListOf<RollingDisputePolicy>()
    for (item in items) {
        val derived = newRollingDisputePolicy(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyRollingDisputePolicy(item: RollingDisputePolicy): String {
    return strictRateCardThresholdBucket(item.netTotal())
}
