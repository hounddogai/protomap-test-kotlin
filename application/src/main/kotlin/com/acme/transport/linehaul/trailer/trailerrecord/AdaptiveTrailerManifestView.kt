package com.acme.transport.linehaul.trailer.trailerrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive trailer manifest. */
data class AdaptiveTrailerManifestView(
    var code: String,
    var title: String,
    var windowDays: String,
    var limit: String,
    var summary: String,
    var statusLabel: String,
)

fun toAdaptiveTrailerManifestView(item: AdaptiveTrailerManifest): AdaptiveTrailerManifestView {
    return AdaptiveTrailerManifestView(
        code = item.code,
        title = item.title,
        windowDays = item.windowDays.toString(),
        limit = item.limit.toString(),
        summary = truncate(item.summary, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTrailerManifestViews(items: MutableList<AdaptiveTrailerManifest>): MutableList<AdaptiveTrailerManifestView> {
    val views = mutableListOf<AdaptiveTrailerManifestView>()
    for (item in items) {
        views.add(toAdaptiveTrailerManifestView(item))
    }
    return views
}

fun sumAdaptiveTrailerManifestView(view: AdaptiveTrailerManifestView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedWindowDays.toDouble() + parsedLimit.toDouble()
}
