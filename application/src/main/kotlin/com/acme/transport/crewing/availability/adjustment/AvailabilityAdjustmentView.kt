package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability adjustment. */
data class AvailabilityAdjustmentView(
    var code: String,
    var title: String,
    var phone: String,
    var vehicleId: String,
    var startsAt: String,
    var endsAtText: String,
    var minimum: String,
    var labelText: String,
    var statusLabel: String,
)

fun toAvailabilityAdjustmentView(item: AvailabilityAdjustment): AvailabilityAdjustmentView {
    return AvailabilityAdjustmentView(
        code = item.code,
        title = truncate(item.title, 120),
        phone = item.phone,
        vehicleId = item.vehicleId,
        startsAt = item.startsAt.toString(),
        endsAtText = item.endsAt.toString(),
        minimum = "%.2f".format(item.minimum),
        labelText = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityAdjustmentViews(items: MutableList<AvailabilityAdjustment>): MutableList<AvailabilityAdjustmentView> {
    val views = mutableListOf<AvailabilityAdjustmentView>()
    for (item in items) {
        views.add(toAvailabilityAdjustmentView(item))
    }
    return views
}

fun sumAvailabilityAdjustmentView(view: AvailabilityAdjustmentView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedStartsAt.toDouble() + parsedEndsAt.toDouble() + parsedMinimum
}
