package com.acme.transport.crewing.roster.threshold

/** How screens and exports show a primary roster digest. */
data class PrimaryRosterDigestView(
    var code: String,
    var nameText: String,
    var minimumText: String,
    var capacityText: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toPrimaryRosterDigestView(item: PrimaryRosterDigest): PrimaryRosterDigestView {
    return PrimaryRosterDigestView(
        code = item.code,
        nameText = item.name,
        minimumText = "%.2f".format(item.minimum),
        capacityText = item.capacity.toString(),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRosterDigestViews(items: MutableList<PrimaryRosterDigest>): MutableList<PrimaryRosterDigestView> {
    val views = mutableListOf<PrimaryRosterDigestView>()
    for (item in items) {
        views.add(toPrimaryRosterDigestView(item))
    }
    return views
}

fun sumPrimaryRosterDigestView(view: PrimaryRosterDigestView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    return parsedMinimum + parsedCapacity.toDouble()
}
