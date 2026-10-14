package com.acme.transport.lastmile.proofofdelivery.cycle

import com.acme.transport.lastmile.redelivery.redeliverytemplate.ScheduledRedeliveryTemplate
import com.acme.transport.lastmile.redelivery.redeliverytemplate.classifyScheduledRedeliveryTemplate

fun summarizeScheduledRedeliveryTemplatesForCycle(items: MutableList<ScheduledRedeliveryTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveRollingProofOfDeliveryCyclesFromTemplate(items: MutableList<ScheduledRedeliveryTemplate>): MutableList<RollingProofOfDeliveryCycle> {
    val out = mutableListOf<RollingProofOfDeliveryCycle>()
    for (item in items) {
        val derived = newRollingProofOfDeliveryCycle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyRollingProofOfDeliveryCycle(item: RollingProofOfDeliveryCycle): String {
    return classifyScheduledRedeliveryTemplate(item.measuredValue())
}
