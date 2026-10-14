package com.acme.transport.crewing.availability.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability plan. */
data class AvailabilityPlanView(
    var code: String,
    var labelText: String,
    var routeCode: String,
    var fuelLiters: String,
    var title: String,
    var visible: String,
    var contactEmail: String,
    var pallets: String,
    var shiftCodeText: String,
    var summaryText: String,
    var limitText: String,
    var statusLabel: String,
)

fun toAvailabilityPlanView(item: AvailabilityPlan): AvailabilityPlanView {
    return AvailabilityPlanView(
        code = item.code,
        labelText = item.label,
        routeCode = truncate(item.routeCode, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        title = item.title,
        visible = "${item.visible}",
        contactEmail = item.contactEmail,
        pallets = item.pallets.toString(),
        shiftCodeText = item.shiftCode,
        summaryText = item.summary,
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityPlanViews(items: MutableList<AvailabilityPlan>): MutableList<AvailabilityPlanView> {
    val views = mutableListOf<AvailabilityPlanView>()
    for (item in items) {
        views.add(toAvailabilityPlanView(item))
    }
    return views
}

fun sumAvailabilityPlanView(view: AvailabilityPlanView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedFuelLiters + parsedPallets.toDouble() + parsedLimit.toDouble()
}
