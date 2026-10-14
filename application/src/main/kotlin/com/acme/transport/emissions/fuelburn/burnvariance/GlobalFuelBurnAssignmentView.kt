package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global fuel burn assignment. */
data class GlobalFuelBurnAssignmentView(
    var codeText: String,
    var nameText: String,
    var margin: String,
    var expiresAt: String,
    var revisionText: String,
    var pallets: String,
    var statusLabel: String,
)

fun toGlobalFuelBurnAssignmentView(item: GlobalFuelBurnAssignment): GlobalFuelBurnAssignmentView {
    return GlobalFuelBurnAssignmentView(
        codeText = truncate(item.code, 64),
        nameText = item.name,
        margin = "%.2f".format(item.margin),
        expiresAt = item.expiresAt.toString(),
        revisionText = item.revision.toString(),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalFuelBurnAssignmentViews(items: MutableList<GlobalFuelBurnAssignment>): MutableList<GlobalFuelBurnAssignmentView> {
    val views = mutableListOf<GlobalFuelBurnAssignmentView>()
    for (item in items) {
        views.add(toGlobalFuelBurnAssignmentView(item))
    }
    return views
}

fun sumGlobalFuelBurnAssignmentView(view: GlobalFuelBurnAssignmentView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedRevision = view.revisionText.toInt()
    return parsedMargin + parsedExpiresAt.toDouble() + parsedRevision.toDouble()
}
