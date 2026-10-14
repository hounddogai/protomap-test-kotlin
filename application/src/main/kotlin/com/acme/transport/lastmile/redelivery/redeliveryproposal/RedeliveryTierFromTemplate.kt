package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.lastmile.redelivery.redeliverytemplate.ScheduledRedeliveryTemplate
import com.acme.transport.lastmile.redelivery.redeliverytemplate.classifyGlobalRedeliveryOverride

fun summarizeScheduledRedeliveryTemplatesForTier(items: MutableList<ScheduledRedeliveryTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveRedeliveryTiersFromTemplate(items: MutableList<ScheduledRedeliveryTemplate>): MutableList<RedeliveryTier> {
    val out = mutableListOf<RedeliveryTier>()
    for (item in items) {
        val derived = newRedeliveryTier(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileRedeliveryTier(item: RedeliveryTier): String = classifyGlobalRedeliveryOverride(item.netValue())
