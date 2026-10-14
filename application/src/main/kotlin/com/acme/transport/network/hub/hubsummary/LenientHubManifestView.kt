package com.acme.transport.network.hub.hubsummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient hub manifest. */
data class LenientHubManifestView(
    var code: String,
    var title: String,
    var floorValue: String,
    var timezone: String,
    var locale: String,
    var statusLabel: String,
)

fun toLenientHubManifestView(item: LenientHubManifest): LenientHubManifestView {
    return LenientHubManifestView(
        code = truncate(item.code, 80),
        title = item.title,
        floorValue = "%.2f".format(item.floorValue),
        timezone = truncate(item.timezone, 64),
        locale = truncate(item.locale, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientHubManifestViews(items: MutableList<LenientHubManifest>): MutableList<LenientHubManifestView> {
    val views = mutableListOf<LenientHubManifestView>()
    for (item in items) {
        views.add(toLenientHubManifestView(item))
    }
    return views
}

fun sumLenientHubManifestView(view: LenientHubManifestView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedFloorValue
}
