package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled fuel surcharge request. */
data class ScheduledFuelSurchargeRequestView(
    var codeText: String,
    var title: String,
    var version: String,
    var category: String,
    var distanceKmText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toScheduledFuelSurchargeRequestView(item: ScheduledFuelSurchargeRequest): ScheduledFuelSurchargeRequestView {
    return ScheduledFuelSurchargeRequestView(
        codeText = truncate(item.code, 120),
        title = item.title,
        version = item.version.toString(),
        category = item.category,
        distanceKmText = "%.2f".format(item.distanceKm),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledFuelSurchargeRequestViews(items: MutableList<ScheduledFuelSurchargeRequest>): MutableList<ScheduledFuelSurchargeRequestView> {
    val views = mutableListOf<ScheduledFuelSurchargeRequestView>()
    for (item in items) {
        views.add(toScheduledFuelSurchargeRequestView(item))
    }
    return views
}

fun sumScheduledFuelSurchargeRequestView(view: ScheduledFuelSurchargeRequestView): Double {
    val parsedVersion = view.version.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedVersion.toDouble() + parsedDistanceKm
}
