package com.acme.transport.linehaul.trailer.traileroverride

/** How screens and exports show a trailer reservation. */
data class TrailerReservationView(
    var codeText: String,
    var name: String,
    var batchSize: String,
    var revision: String,
    var summaryText: String,
    var archivedText: String,
    var fuelLitersText: String,
    var discount: String,
    var maximum: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toTrailerReservationView(item: TrailerReservation): TrailerReservationView {
    return TrailerReservationView(
        codeText = item.code,
        name = item.name,
        batchSize = item.batchSize.toString(),
        revision = item.revision.toString(),
        summaryText = item.summary,
        archivedText = "${item.archived}",
        fuelLitersText = "%.2f".format(item.fuelLiters),
        discount = "%.2f".format(item.discount),
        maximum = "%.2f".format(item.maximum),
        carrierText = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerReservationViews(items: MutableList<TrailerReservation>): MutableList<TrailerReservationView> {
    val views = mutableListOf<TrailerReservationView>()
    for (item in items) {
        views.add(toTrailerReservationView(item))
    }
    return views
}

fun sumTrailerReservationView(view: TrailerReservationView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedBatchSize.toDouble() + parsedRevision.toDouble() + parsedFuelLiters
}
