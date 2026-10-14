package com.acme.transport.network.node.nodegroup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy node assignment. */
data class LegacyNodeAssignmentView(
    var codeText: String,
    var nameText: String,
    var summaryText: String,
    var updatedAt: String,
    var odometerText: String,
    var limit: String,
    var archived: String,
    var windowDays: String,
    var externalRef: String,
    var weightText: String,
    var channel: String,
    var statusLabel: String,
)

fun toLegacyNodeAssignmentView(item: LegacyNodeAssignment): LegacyNodeAssignmentView {
    return LegacyNodeAssignmentView(
        codeText = truncate(item.code, 120),
        nameText = item.name,
        summaryText = truncate(item.summary, 40),
        updatedAt = item.updatedAt.toString(),
        odometerText = item.odometer.toString(),
        limit = item.limit.toString(),
        archived = "${item.archived}",
        windowDays = item.windowDays.toString(),
        externalRef = item.externalRef,
        weightText = "%.2f".format(item.weight),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyNodeAssignmentViews(items: MutableList<LegacyNodeAssignment>): MutableList<LegacyNodeAssignmentView> {
    val views = mutableListOf<LegacyNodeAssignmentView>()
    for (item in items) {
        views.add(toLegacyNodeAssignmentView(item))
    }
    return views
}

fun sumLegacyNodeAssignmentView(view: LegacyNodeAssignmentView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedOdometer = view.odometerText.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedUpdatedAt.toDouble() + parsedOdometer.toDouble() + parsedLimit.toDouble()
}
