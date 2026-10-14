package com.acme.transport.network.cutoff.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped cutoff manifest. */
data class CappedCutoffManifestView(
    var codeText: String,
    var name: String,
    var limit: String,
    var updatedAtText: String,
    var ownerId: String,
    var visible: String,
    var driverId: String,
    var odometer: String,
    var loadKgText: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toCappedCutoffManifestView(item: CappedCutoffManifest): CappedCutoffManifestView {
    return CappedCutoffManifestView(
        codeText = item.code,
        name = item.name,
        limit = item.limit.toString(),
        updatedAtText = item.updatedAt.toString(),
        ownerId = truncate(item.ownerId, 120),
        visible = "${item.visible}",
        driverId = truncate(item.driverId, 80),
        odometer = item.odometer.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        descriptionText = truncate(item.description, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCutoffManifestViews(items: MutableList<CappedCutoffManifest>): MutableList<CappedCutoffManifestView> {
    val views = mutableListOf<CappedCutoffManifestView>()
    for (item in items) {
        views.add(toCappedCutoffManifestView(item))
    }
    return views
}

fun sumCappedCutoffManifestView(view: CappedCutoffManifestView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedLimit.toDouble() + parsedUpdatedAt.toDouble() + parsedOdometer.toDouble()
}
