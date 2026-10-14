package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn assignment. */
data class FuelBurnAssignmentView(
    var code: String,
    var label: String,
    var checksum: String,
    var odometer: String,
    var description: String,
    var ratio: String,
    var retries: String,
    var statusLabel: String,
)

fun toFuelBurnAssignmentView(item: FuelBurnAssignment): FuelBurnAssignmentView {
    return FuelBurnAssignmentView(
        code = truncate(item.code, 80),
        label = truncate(item.label, 80),
        checksum = item.checksum,
        odometer = item.odometer.toString(),
        description = truncate(item.description, 64),
        ratio = "%.2f".format(item.ratio),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnAssignmentViews(items: MutableList<FuelBurnAssignment>): MutableList<FuelBurnAssignmentView> {
    val views = mutableListOf<FuelBurnAssignmentView>()
    for (item in items) {
        views.add(toFuelBurnAssignmentView(item))
    }
    return views
}

fun sumFuelBurnAssignmentView(view: FuelBurnAssignmentView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedOdometer.toDouble() + parsedRatio + parsedRetries.toDouble()
}
