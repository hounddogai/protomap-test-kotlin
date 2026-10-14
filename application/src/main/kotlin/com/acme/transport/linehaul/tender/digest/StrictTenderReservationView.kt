package com.acme.transport.linehaul.tender.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict tender reservation. */
data class StrictTenderReservationView(
    var code: String,
    var title: String,
    var lane: String,
    var weightText: String,
    var revision: String,
    var loadKgText: String,
    var discount: String,
    var categoryText: String,
    var floorValue: String,
    var notes: String,
    var statusLabel: String,
)

fun toStrictTenderReservationView(item: StrictTenderReservation): StrictTenderReservationView {
    return StrictTenderReservationView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 120),
        lane = item.lane,
        weightText = "%.2f".format(item.weight),
        revision = item.revision.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        discount = "%.2f".format(item.discount),
        categoryText = truncate(item.category, 120),
        floorValue = "%.2f".format(item.floorValue),
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTenderReservationViews(items: MutableList<StrictTenderReservation>): MutableList<StrictTenderReservationView> {
    val views = mutableListOf<StrictTenderReservationView>()
    for (item in items) {
        views.add(toStrictTenderReservationView(item))
    }
    return views
}

fun sumStrictTenderReservationView(view: StrictTenderReservationView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedWeight + parsedRevision.toDouble() + parsedLoadKg
}
