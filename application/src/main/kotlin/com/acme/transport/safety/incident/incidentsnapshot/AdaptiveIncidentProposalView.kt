package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive incident proposal. */
data class AdaptiveIncidentProposalView(
    var code: String,
    var labelText: String,
    var timezone: String,
    var shiftCodeText: String,
    var cost: String,
    var threshold: String,
    var depotText: String,
    var statusLabel: String,
)

fun toAdaptiveIncidentProposalView(item: AdaptiveIncidentProposal): AdaptiveIncidentProposalView {
    return AdaptiveIncidentProposalView(
        code = truncate(item.code, 40),
        labelText = item.label,
        timezone = item.timezone,
        shiftCodeText = item.shiftCode,
        cost = "%.2f".format(item.cost),
        threshold = "%.2f".format(item.threshold),
        depotText = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveIncidentProposalViews(items: MutableList<AdaptiveIncidentProposal>): MutableList<AdaptiveIncidentProposalView> {
    val views = mutableListOf<AdaptiveIncidentProposalView>()
    for (item in items) {
        views.add(toAdaptiveIncidentProposalView(item))
    }
    return views
}

fun sumAdaptiveIncidentProposalView(view: AdaptiveIncidentProposalView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedCost + parsedThreshold
}
