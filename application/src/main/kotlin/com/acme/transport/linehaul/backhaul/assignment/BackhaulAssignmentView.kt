package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul assignment. */
data class BackhaulAssignmentView(
    var codeText: String,
    var label: String,
    var dock: String,
    var etaMinutes: String,
    var weight: String,
    var thresholdText: String,
    var activeText: String,
    var amount: String,
    var endsAt: String,
    var laneText: String,
    var depotText: String,
    var statusLabel: String,
)

fun toBackhaulAssignmentView(item: BackhaulAssignment): BackhaulAssignmentView {
    return BackhaulAssignmentView(
        codeText = item.code,
        label = item.label,
        dock = item.dock,
        etaMinutes = item.etaMinutes.toString(),
        weight = "%.2f".format(item.weight),
        thresholdText = "%.2f".format(item.threshold),
        activeText = "${item.active}",
        amount = "%.2f".format(item.amount),
        endsAt = item.endsAt.toString(),
        laneText = truncate(item.lane, 40),
        depotText = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulAssignmentViews(items: MutableList<BackhaulAssignment>): MutableList<BackhaulAssignmentView> {
    val views = mutableListOf<BackhaulAssignmentView>()
    for (item in items) {
        views.add(toBackhaulAssignmentView(item))
    }
    return views
}

fun sumBackhaulAssignmentView(view: BackhaulAssignmentView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedWeight + parsedThreshold
}
