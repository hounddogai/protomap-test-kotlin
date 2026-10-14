package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel manifest. */
data class ParcelManifestView(
    var code: String,
    var label: String,
    var driverId: String,
    var externalRefText: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toParcelManifestView(item: ParcelManifest): ParcelManifestView {
    return ParcelManifestView(
        code = item.code,
        label = item.label,
        driverId = truncate(item.driverId, 40),
        externalRefText = truncate(item.externalRef, 120),
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelManifestViews(items: MutableList<ParcelManifest>): MutableList<ParcelManifestView> {
    val views = mutableListOf<ParcelManifestView>()
    for (item in items) {
        views.add(toParcelManifestView(item))
    }
    return views
}

fun sumParcelManifestView(view: ParcelManifestView): Double {
    val parsedRetries = view.retriesText.toInt()
    return parsedRetries.toDouble()
}
