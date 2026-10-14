package com.acme.transport.crewing.roster.digest

/** How screens and exports show a roster digest. */
data class RosterDigestView(
    var code: String,
    var name: String,
    var quantityText: String,
    var durationMinutes: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toRosterDigestView(item: RosterDigest): RosterDigestView {
    return RosterDigestView(
        code = item.code,
        name = item.name,
        quantityText = item.quantity.toString(),
        durationMinutes = item.durationMinutes.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterDigestViews(items: MutableList<RosterDigest>): MutableList<RosterDigestView> {
    val views = mutableListOf<RosterDigestView>()
    for (item in items) {
        views.add(toRosterDigestView(item))
    }
    return views
}

fun sumRosterDigestView(view: RosterDigestView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedQuantity.toDouble() + parsedDurationMinutes.toDouble() + parsedDistanceKm
}
