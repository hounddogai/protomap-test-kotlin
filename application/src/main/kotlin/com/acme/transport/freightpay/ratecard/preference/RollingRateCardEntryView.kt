package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling rate card entry. */
data class RollingRateCardEntryView(
    var code: String,
    var title: String,
    var categoryText: String,
    var odometer: String,
    var externalRef: String,
    var floorValueText: String,
    var endsAt: String,
    var distanceKm: String,
    var expiresAt: String,
    var contactEmail: String,
    var capacity: String,
    var statusLabel: String,
)

fun toRollingRateCardEntryView(item: RollingRateCardEntry): RollingRateCardEntryView {
    return RollingRateCardEntryView(
        code = item.code,
        title = item.title,
        categoryText = truncate(item.category, 40),
        odometer = item.odometer.toString(),
        externalRef = item.externalRef,
        floorValueText = "%.2f".format(item.floorValue),
        endsAt = item.endsAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        expiresAt = item.expiresAt.toString(),
        contactEmail = item.contactEmail,
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRateCardEntryViews(items: MutableList<RollingRateCardEntry>): MutableList<RollingRateCardEntryView> {
    val views = mutableListOf<RollingRateCardEntryView>()
    for (item in items) {
        views.add(toRollingRateCardEntryView(item))
    }
    return views
}

fun sumRollingRateCardEntryView(view: RollingRateCardEntryView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedOdometer.toDouble() + parsedFloorValue + parsedEndsAt.toDouble()
}
