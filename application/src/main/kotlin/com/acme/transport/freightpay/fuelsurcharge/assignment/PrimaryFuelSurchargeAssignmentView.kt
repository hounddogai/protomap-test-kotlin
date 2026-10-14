package com.acme.transport.freightpay.fuelsurcharge.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary fuel surcharge assignment. */
data class PrimaryFuelSurchargeAssignmentView(
    var codeText: String,
    var label: String,
    var fuelLiters: String,
    var visible: String,
    var carrier: String,
    var endsAt: String,
    var title: String,
    var windowDaysText: String,
    var floorValueText: String,
    var checksumText: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toPrimaryFuelSurchargeAssignmentView(item: PrimaryFuelSurchargeAssignment): PrimaryFuelSurchargeAssignmentView {
    return PrimaryFuelSurchargeAssignmentView(
        codeText = item.code,
        label = item.label,
        fuelLiters = "%.2f".format(item.fuelLiters),
        visible = "${item.visible}",
        carrier = item.carrier,
        endsAt = item.endsAt.toString(),
        title = item.title,
        windowDaysText = item.windowDays.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        checksumText = truncate(item.checksum, 80),
        vehicleId = truncate(item.vehicleId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryFuelSurchargeAssignmentViews(items: MutableList<PrimaryFuelSurchargeAssignment>): MutableList<PrimaryFuelSurchargeAssignmentView> {
    val views = mutableListOf<PrimaryFuelSurchargeAssignmentView>()
    for (item in items) {
        views.add(toPrimaryFuelSurchargeAssignmentView(item))
    }
    return views
}

fun sumPrimaryFuelSurchargeAssignmentView(view: PrimaryFuelSurchargeAssignmentView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedFuelLiters + parsedEndsAt.toDouble() + parsedWindowDays.toDouble()
}
