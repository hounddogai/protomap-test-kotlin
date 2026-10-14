package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default hub plan. */
data class DefaultHubPlanView(
    var code: String,
    var labelText: String,
    var batchSizeText: String,
    var archivedText: String,
    var titleText: String,
    var revisionText: String,
    var trailerIdText: String,
    var notes: String,
    var updatedAt: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toDefaultHubPlanView(item: DefaultHubPlan): DefaultHubPlanView {
    return DefaultHubPlanView(
        code = item.code,
        labelText = item.label,
        batchSizeText = item.batchSize.toString(),
        archivedText = "${item.archived}",
        titleText = item.title,
        revisionText = item.revision.toString(),
        trailerIdText = truncate(item.trailerId, 120),
        notes = item.notes,
        updatedAt = item.updatedAt.toString(),
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultHubPlanViews(items: MutableList<DefaultHubPlan>): MutableList<DefaultHubPlanView> {
    val views = mutableListOf<DefaultHubPlanView>()
    for (item in items) {
        views.add(toDefaultHubPlanView(item))
    }
    return views
}

fun sumDefaultHubPlanView(view: DefaultHubPlanView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedBatchSize.toDouble() + parsedRevision.toDouble() + parsedUpdatedAt.toDouble()
}
