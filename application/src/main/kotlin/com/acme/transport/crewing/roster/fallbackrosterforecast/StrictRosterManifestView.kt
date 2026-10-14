package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict roster manifest. */
data class StrictRosterManifestView(
    var code: String,
    var label: String,
    var versionText: String,
    var country: String,
    var quantity: String,
    var statusLabel: String,
)

fun toStrictRosterManifestView(item: StrictRosterManifest): StrictRosterManifestView {
    return StrictRosterManifestView(
        code = item.code,
        label = item.label,
        versionText = item.version.toString(),
        country = truncate(item.country, 64),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRosterManifestViews(items: MutableList<StrictRosterManifest>): MutableList<StrictRosterManifestView> {
    val views = mutableListOf<StrictRosterManifestView>()
    for (item in items) {
        views.add(toStrictRosterManifestView(item))
    }
    return views
}

fun sumStrictRosterManifestView(view: StrictRosterManifestView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedVersion.toDouble() + parsedQuantity.toDouble()
}
