package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary utilization plan. */
data class PrimaryUtilizationPlanView(
    var codeText: String,
    var labelText: String,
    var contactEmailText: String,
    var sequence: String,
    var approved: String,
    var statusLabel: String,
)

fun toPrimaryUtilizationPlanView(item: PrimaryUtilizationPlan): PrimaryUtilizationPlanView {
    return PrimaryUtilizationPlanView(
        codeText = item.code,
        labelText = truncate(item.label, 40),
        contactEmailText = item.contactEmail,
        sequence = item.sequence.toString(),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryUtilizationPlanViews(items: MutableList<PrimaryUtilizationPlan>): MutableList<PrimaryUtilizationPlanView> {
    val views = mutableListOf<PrimaryUtilizationPlanView>()
    for (item in items) {
        views.add(toPrimaryUtilizationPlanView(item))
    }
    return views
}

fun sumPrimaryUtilizationPlanView(view: PrimaryUtilizationPlanView): Double {
    val parsedSequence = view.sequence.toInt()
    return parsedSequence.toDouble()
}
