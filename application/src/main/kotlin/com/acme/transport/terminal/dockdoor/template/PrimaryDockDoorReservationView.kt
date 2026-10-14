package com.acme.transport.terminal.dockdoor.template

/** How screens and exports show a primary dock door reservation. */
data class PrimaryDockDoorReservationView(
    var code: String,
    var label: String,
    var batchSizeText: String,
    var segmentText: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toPrimaryDockDoorReservationView(item: PrimaryDockDoorReservation): PrimaryDockDoorReservationView {
    return PrimaryDockDoorReservationView(
        code = item.code,
        label = item.label,
        batchSizeText = item.batchSize.toString(),
        segmentText = item.segment,
        currencyText = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDockDoorReservationViews(items: MutableList<PrimaryDockDoorReservation>): MutableList<PrimaryDockDoorReservationView> {
    val views = mutableListOf<PrimaryDockDoorReservationView>()
    for (item in items) {
        views.add(toPrimaryDockDoorReservationView(item))
    }
    return views
}

fun sumPrimaryDockDoorReservationView(view: PrimaryDockDoorReservationView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedBatchSize.toDouble()
}
