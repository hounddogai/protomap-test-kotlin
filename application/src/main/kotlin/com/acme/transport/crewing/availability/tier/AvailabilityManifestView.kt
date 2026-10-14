package com.acme.transport.crewing.availability.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability manifest. */
data class AvailabilityManifestView(
    var code: String,
    var name: String,
    var minimumText: String,
    var attempts: String,
    var currencyText: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toAvailabilityManifestView(item: AvailabilityManifest): AvailabilityManifestView {
    return AvailabilityManifestView(
        code = item.code,
        name = truncate(item.name, 64),
        minimumText = "%.2f".format(item.minimum),
        attempts = item.attempts.toString(),
        currencyText = truncate(item.currency, 80),
        checksumText = truncate(item.checksum, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityManifestViews(items: MutableList<AvailabilityManifest>): MutableList<AvailabilityManifestView> {
    val views = mutableListOf<AvailabilityManifestView>()
    for (item in items) {
        views.add(toAvailabilityManifestView(item))
    }
    return views
}

fun sumAvailabilityManifestView(view: AvailabilityManifestView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedMinimum + parsedAttempts.toDouble()
}
