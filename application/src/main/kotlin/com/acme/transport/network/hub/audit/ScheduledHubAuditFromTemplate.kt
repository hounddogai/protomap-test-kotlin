package com.acme.transport.network.hub.audit

import com.acme.transport.network.cutoff.template.LenientCutoffTemplate
import com.acme.transport.network.cutoff.template.normalizeLenientCutoffTemplateCode
import com.acme.transport.network.hub.template.PendingHubTemplate
import com.acme.transport.network.hub.template.classifyHubScore

fun summarizeLenientCutoffTemplatesForAudit(items: MutableList<LenientCutoffTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveScheduledHubAuditsFromTemplate(items: MutableList<LenientCutoffTemplate>): MutableList<ScheduledHubAudit> {
    val out = mutableListOf<ScheduledHubAudit>()
    for (item in items) {
        val derived = newScheduledHubAudit(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreScheduledHubAudit(item: ScheduledHubAudit): String = normalizeLenientCutoffTemplateCode(item.code)

fun summarizePendingHubTemplatesForAudit(items: MutableList<PendingHubTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveScheduledHubAuditsFromTemplateExtra(items: MutableList<PendingHubTemplate>): MutableList<ScheduledHubAudit> {
    val out = mutableListOf<ScheduledHubAudit>()
    for (item in items) {
        val derived = newScheduledHubAudit(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreScheduledHubAuditExtra(item: ScheduledHubAudit): String = classifyHubScore(item.measuredValue())
