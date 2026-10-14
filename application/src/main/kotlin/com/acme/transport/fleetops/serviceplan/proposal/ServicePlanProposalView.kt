package com.acme.transport.fleetops.serviceplan.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan proposal. */
data class ServicePlanProposalView(
    var code: String,
    var label: String,
    var priority: String,
    var maximum: String,
    var locale: String,
    var score: String,
    var statusLabel: String,
)

fun toServicePlanProposalView(item: ServicePlanProposal): ServicePlanProposalView {
    return ServicePlanProposalView(
        code = truncate(item.code, 40),
        label = truncate(item.label, 120),
        priority = item.priority.toString(),
        maximum = "%.2f".format(item.maximum),
        locale = truncate(item.locale, 64),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanProposalViews(items: MutableList<ServicePlanProposal>): MutableList<ServicePlanProposalView> {
    val views = mutableListOf<ServicePlanProposalView>()
    for (item in items) {
        views.add(toServicePlanProposalView(item))
    }
    return views
}

fun sumServicePlanProposalView(view: ServicePlanProposalView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedPriority.toDouble() + parsedMaximum + parsedScore
}
