package com.acme.transport.lastmile.parcel.scheduledparcelreservation

/** How screens and exports show a seasonal parcel reservation. */
data class SeasonalParcelReservationView(
    var codeText: String,
    var labelText: String,
    var notes: String,
    var nameText: String,
    var costText: String,
    var country: String,
    var ceiling: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toSeasonalParcelReservationView(item: SeasonalParcelReservation): SeasonalParcelReservationView {
    return SeasonalParcelReservationView(
        codeText = item.code,
        labelText = item.label,
        notes = item.notes,
        nameText = item.name,
        costText = "%.2f".format(item.cost),
        country = item.country,
        ceiling = "%.2f".format(item.ceiling),
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalParcelReservationViews(items: MutableList<SeasonalParcelReservation>): MutableList<SeasonalParcelReservationView> {
    val views = mutableListOf<SeasonalParcelReservationView>()
    for (item in items) {
        views.add(toSeasonalParcelReservationView(item))
    }
    return views
}

fun sumSeasonalParcelReservationView(view: SeasonalParcelReservationView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedCost + parsedCeiling
}
