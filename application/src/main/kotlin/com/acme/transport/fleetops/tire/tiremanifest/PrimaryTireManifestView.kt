package com.acme.transport.fleetops.tire.tiremanifest

/** How screens and exports show a primary tire manifest. */
data class PrimaryTireManifestView(
    var codeText: String,
    var label: String,
    var vehicleId: String,
    var discountText: String,
    var minimum: String,
    var batchSizeText: String,
    var dockText: String,
    var updatedAtText: String,
    var expiresAt: String,
    var marginText: String,
    var statusLabel: String,
)

fun toPrimaryTireManifestView(item: PrimaryTireManifest): PrimaryTireManifestView {
    return PrimaryTireManifestView(
        codeText = item.code,
        label = item.label,
        vehicleId = item.vehicleId,
        discountText = "%.2f".format(item.discount),
        minimum = "%.2f".format(item.minimum),
        batchSizeText = item.batchSize.toString(),
        dockText = item.dock,
        updatedAtText = item.updatedAt.toString(),
        expiresAt = item.expiresAt.toString(),
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTireManifestViews(items: MutableList<PrimaryTireManifest>): MutableList<PrimaryTireManifestView> {
    val views = mutableListOf<PrimaryTireManifestView>()
    for (item in items) {
        views.add(toPrimaryTireManifestView(item))
    }
    return views
}

fun sumPrimaryTireManifestView(view: PrimaryTireManifestView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedDiscount + parsedMinimum + parsedBatchSize.toDouble()
}
