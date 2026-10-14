package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default sortation reservation. */
data class DefaultSortationReservationView(
    var code: String,
    var title: String,
    var carrierText: String,
    var dock: String,
    var visible: String,
    var notesText: String,
    var distanceKmText: String,
    var weight: String,
    var statusLabel: String,
)

fun toDefaultSortationReservationView(item: DefaultSortationReservation): DefaultSortationReservationView {
    return DefaultSortationReservationView(
        code = item.code,
        title = truncate(item.title, 80),
        carrierText = item.carrier,
        dock = truncate(item.dock, 80),
        visible = "${item.visible}",
        notesText = item.notes,
        distanceKmText = "%.2f".format(item.distanceKm),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultSortationReservationViews(items: MutableList<DefaultSortationReservation>): MutableList<DefaultSortationReservationView> {
    val views = mutableListOf<DefaultSortationReservationView>()
    for (item in items) {
        views.add(toDefaultSortationReservationView(item))
    }
    return views
}

fun sumDefaultSortationReservationView(view: DefaultSortationReservationView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedDistanceKm + parsedWeight
}
