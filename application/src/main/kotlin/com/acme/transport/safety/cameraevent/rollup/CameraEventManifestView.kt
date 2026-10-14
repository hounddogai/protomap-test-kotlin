package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event manifest. */
data class CameraEventManifestView(
    var code: String,
    var name: String,
    var segment: String,
    var carrier: String,
    var regionText: String,
    var ownerId: String,
    var country: String,
    var shiftCode: String,
    var trailerId: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toCameraEventManifestView(item: CameraEventManifest): CameraEventManifestView {
    return CameraEventManifestView(
        code = truncate(item.code, 120),
        name = truncate(item.name, 40),
        segment = item.segment,
        carrier = item.carrier,
        regionText = item.region,
        ownerId = truncate(item.ownerId, 40),
        country = truncate(item.country, 64),
        shiftCode = truncate(item.shiftCode, 64),
        trailerId = truncate(item.trailerId, 80),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventManifestViews(items: MutableList<CameraEventManifest>): MutableList<CameraEventManifestView> {
    val views = mutableListOf<CameraEventManifestView>()
    for (item in items) {
        views.add(toCameraEventManifestView(item))
    }
    return views
}

fun sumCameraEventManifestView(view: CameraEventManifestView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
