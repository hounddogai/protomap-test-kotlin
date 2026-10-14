package com.acme.transport.linehaul.capacity.capacityreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered capacity reservation. */
data class TieredCapacityReservationView(
    var code: String,
    var labelText: String,
    var country: String,
    var contactEmail: String,
    var discount: String,
    var threshold: String,
    var source: String,
    var statusLabel: String,
)

fun toTieredCapacityReservationView(item: TieredCapacityReservation): TieredCapacityReservationView {
    return TieredCapacityReservationView(
        code = item.code,
        labelText = item.label,
        country = truncate(item.country, 64),
        contactEmail = item.contactEmail,
        discount = "%.2f".format(item.discount),
        threshold = "%.2f".format(item.threshold),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCapacityReservationViews(items: MutableList<TieredCapacityReservation>): MutableList<TieredCapacityReservationView> {
    val views = mutableListOf<TieredCapacityReservationView>()
    for (item in items) {
        views.add(toTieredCapacityReservationView(item))
    }
    return views
}

fun sumTieredCapacityReservationView(view: TieredCapacityReservationView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedDiscount + parsedThreshold
}
