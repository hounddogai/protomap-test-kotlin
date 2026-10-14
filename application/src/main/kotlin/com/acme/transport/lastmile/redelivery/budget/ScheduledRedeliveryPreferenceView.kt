package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled redelivery preference. */
data class ScheduledRedeliveryPreferenceView(
    var code: String,
    var name: String,
    var driverId: String,
    var ownerIdText: String,
    var odometer: String,
    var statusLabel: String,
)

fun toScheduledRedeliveryPreferenceView(item: ScheduledRedeliveryPreference): ScheduledRedeliveryPreferenceView {
    return ScheduledRedeliveryPreferenceView(
        code = item.code,
        name = truncate(item.name, 64),
        driverId = item.driverId,
        ownerIdText = truncate(item.ownerId, 40),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledRedeliveryPreferenceViews(items: MutableList<ScheduledRedeliveryPreference>): MutableList<ScheduledRedeliveryPreferenceView> {
    val views = mutableListOf<ScheduledRedeliveryPreferenceView>()
    for (item in items) {
        views.add(toScheduledRedeliveryPreferenceView(item))
    }
    return views
}

fun sumScheduledRedeliveryPreferenceView(view: ScheduledRedeliveryPreferenceView): Double {
    val parsedOdometer = view.odometer.toInt()
    return parsedOdometer.toDouble()
}
