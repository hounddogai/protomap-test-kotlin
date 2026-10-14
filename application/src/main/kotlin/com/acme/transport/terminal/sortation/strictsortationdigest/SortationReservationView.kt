package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation reservation. */
data class SortationReservationView(
    var codeText: String,
    var title: String,
    var dockText: String,
    var createdAt: String,
    var channel: String,
    var quantityText: String,
    var updatedAtText: String,
    var checksum: String,
    var approved: String,
    var ownerId: String,
    var localeText: String,
    var statusLabel: String,
)

fun toSortationReservationView(item: SortationReservation): SortationReservationView {
    return SortationReservationView(
        codeText = truncate(item.code, 120),
        title = item.title,
        dockText = truncate(item.dock, 40),
        createdAt = item.createdAt.toString(),
        channel = truncate(item.channel, 40),
        quantityText = item.quantity.toString(),
        updatedAtText = item.updatedAt.toString(),
        checksum = item.checksum,
        approved = "${item.approved}",
        ownerId = item.ownerId,
        localeText = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationReservationViews(items: MutableList<SortationReservation>): MutableList<SortationReservationView> {
    val views = mutableListOf<SortationReservationView>()
    for (item in items) {
        views.add(toSortationReservationView(item))
    }
    return views
}

fun sumSortationReservationView(view: SortationReservationView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedQuantity = view.quantityText.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedCreatedAt.toDouble() + parsedQuantity.toDouble() + parsedUpdatedAt.toDouble()
}
