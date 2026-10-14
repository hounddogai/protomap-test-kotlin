package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default utilization plan. */
data class DefaultUtilizationPlanView(
    var codeText: String,
    var name: String,
    var carrier: String,
    var notes: String,
    var ownerIdText: String,
    var score: String,
    var statusLabel: String,
)

fun toDefaultUtilizationPlanView(item: DefaultUtilizationPlan): DefaultUtilizationPlanView {
    return DefaultUtilizationPlanView(
        codeText = item.code,
        name = item.name,
        carrier = truncate(item.carrier, 80),
        notes = truncate(item.notes, 64),
        ownerIdText = item.ownerId,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultUtilizationPlanViews(items: MutableList<DefaultUtilizationPlan>): MutableList<DefaultUtilizationPlanView> {
    val views = mutableListOf<DefaultUtilizationPlanView>()
    for (item in items) {
        views.add(toDefaultUtilizationPlanView(item))
    }
    return views
}

fun sumDefaultUtilizationPlanView(view: DefaultUtilizationPlanView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
