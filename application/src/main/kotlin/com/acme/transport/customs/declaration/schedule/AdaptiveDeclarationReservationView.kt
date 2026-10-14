package com.acme.transport.customs.declaration.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive declaration reservation. */
data class AdaptiveDeclarationReservationView(
    var codeText: String,
    var nameText: String,
    var ownerId: String,
    var quantity: String,
    var regionText: String,
    var pinned: String,
    var capacity: String,
    var country: String,
    var visible: String,
    var minimum: String,
    var statusLabel: String,
)

fun toAdaptiveDeclarationReservationView(item: AdaptiveDeclarationReservation): AdaptiveDeclarationReservationView {
    return AdaptiveDeclarationReservationView(
        codeText = item.code,
        nameText = item.name,
        ownerId = truncate(item.ownerId, 120),
        quantity = item.quantity.toString(),
        regionText = truncate(item.region, 40),
        pinned = "${item.pinned}",
        capacity = item.capacity.toString(),
        country = item.country,
        visible = "${item.visible}",
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveDeclarationReservationViews(items: MutableList<AdaptiveDeclarationReservation>): MutableList<AdaptiveDeclarationReservationView> {
    val views = mutableListOf<AdaptiveDeclarationReservationView>()
    for (item in items) {
        views.add(toAdaptiveDeclarationReservationView(item))
    }
    return views
}

fun sumAdaptiveDeclarationReservationView(view: AdaptiveDeclarationReservationView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedQuantity.toDouble() + parsedCapacity.toDouble() + parsedMinimum
}
