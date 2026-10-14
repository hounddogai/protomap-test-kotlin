package com.acme.transport.network.zone.zonemapping

/** How screens and exports show a zone assignment. */
data class ZoneAssignmentView(
    var code: String,
    var label: String,
    var quantityText: String,
    var distanceKmText: String,
    var ceiling: String,
    var floorValue: String,
    var threshold: String,
    var depotText: String,
    var fuelLitersText: String,
    var discount: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toZoneAssignmentView(item: ZoneAssignment): ZoneAssignmentView {
    return ZoneAssignmentView(
        code = item.code,
        label = item.label,
        quantityText = item.quantity.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        ceiling = "%.2f".format(item.ceiling),
        floorValue = "%.2f".format(item.floorValue),
        threshold = "%.2f".format(item.threshold),
        depotText = item.depot,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        discount = "%.2f".format(item.discount),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneAssignmentViews(items: MutableList<ZoneAssignment>): MutableList<ZoneAssignmentView> {
    val views = mutableListOf<ZoneAssignmentView>()
    for (item in items) {
        views.add(toZoneAssignmentView(item))
    }
    return views
}

fun sumZoneAssignmentView(view: ZoneAssignmentView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedQuantity.toDouble() + parsedDistanceKm + parsedCeiling
}
