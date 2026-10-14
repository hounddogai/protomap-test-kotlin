package com.acme.transport.linehaul.relay.relayquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled relay rule. */
data class ScheduledRelayRuleView(
    var code: String,
    var labelText: String,
    var revisionText: String,
    var etaMinutes: String,
    var loadKg: String,
    var budget: String,
    var graceDays: String,
    var segmentText: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toScheduledRelayRuleView(item: ScheduledRelayRule): ScheduledRelayRuleView {
    return ScheduledRelayRuleView(
        code = truncate(item.code, 64),
        labelText = item.label,
        revisionText = item.revision.toString(),
        etaMinutes = item.etaMinutes.toString(),
        loadKg = "%.2f".format(item.loadKg),
        budget = "%.2f".format(item.budget),
        graceDays = item.graceDays.toString(),
        segmentText = item.segment,
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledRelayRuleViews(items: MutableList<ScheduledRelayRule>): MutableList<ScheduledRelayRuleView> {
    val views = mutableListOf<ScheduledRelayRuleView>()
    for (item in items) {
        views.add(toScheduledRelayRuleView(item))
    }
    return views
}

fun sumScheduledRelayRuleView(view: ScheduledRelayRuleView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedRevision.toDouble() + parsedEtaMinutes.toDouble() + parsedLoadKg
}
