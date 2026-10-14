package com.acme.transport.lastmile.redelivery.redeliveryclaim

import com.acme.transport.lastmile.redelivery.budget.ScheduledRedeliveryPreference
import com.acme.transport.lastmile.redelivery.budget.globalRedeliveryBundleBucket

fun summarizeScheduledRedeliveryPreferencesForClaim(items: MutableList<ScheduledRedeliveryPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryRedeliveryClaimsFromPreference(items: MutableList<ScheduledRedeliveryPreference>): MutableList<PrimaryRedeliveryClaim> {
    val out = mutableListOf<PrimaryRedeliveryClaim>()
    for (item in items) {
        val derived = newPrimaryRedeliveryClaim(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitPrimaryRedeliveryClaim(item: PrimaryRedeliveryClaim): String {
    return globalRedeliveryBundleBucket(item.weightedVolume())
}
