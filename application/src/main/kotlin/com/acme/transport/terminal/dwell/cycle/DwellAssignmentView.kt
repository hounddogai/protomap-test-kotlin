package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell assignment. */
data class DwellAssignmentView(
    var codeText: String,
    var labelText: String,
    var minimumText: String,
    var odometer: String,
    var pinned: String,
    var discount: String,
    var version: String,
    var depot: String,
    var weight: String,
    var statusLabel: String,
)

fun toDwellAssignmentView(item: DwellAssignment): DwellAssignmentView {
    return DwellAssignmentView(
        codeText = item.code,
        labelText = truncate(item.label, 80),
        minimumText = "%.2f".format(item.minimum),
        odometer = item.odometer.toString(),
        pinned = "${item.pinned}",
        discount = "%.2f".format(item.discount),
        version = item.version.toString(),
        depot = item.depot,
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellAssignmentViews(items: MutableList<DwellAssignment>): MutableList<DwellAssignmentView> {
    val views = mutableListOf<DwellAssignmentView>()
    for (item in items) {
        views.add(toDwellAssignmentView(item))
    }
    return views
}

fun sumDwellAssignmentView(view: DwellAssignmentView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedOdometer = view.odometer.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedMinimum + parsedOdometer.toDouble() + parsedDiscount
}
