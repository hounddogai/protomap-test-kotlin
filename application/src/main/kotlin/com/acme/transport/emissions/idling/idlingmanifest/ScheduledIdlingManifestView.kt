package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled idling manifest. */
data class ScheduledIdlingManifestView(
    var code: String,
    var title: String,
    var marginText: String,
    var loadKg: String,
    var description: String,
    var label: String,
    var pallets: String,
    var maximum: String,
    var statusLabel: String,
)

fun toScheduledIdlingManifestView(item: ScheduledIdlingManifest): ScheduledIdlingManifestView {
    return ScheduledIdlingManifestView(
        code = item.code,
        title = item.title,
        marginText = "%.2f".format(item.margin),
        loadKg = "%.2f".format(item.loadKg),
        description = item.description,
        label = truncate(item.label, 40),
        pallets = item.pallets.toString(),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledIdlingManifestViews(items: MutableList<ScheduledIdlingManifest>): MutableList<ScheduledIdlingManifestView> {
    val views = mutableListOf<ScheduledIdlingManifestView>()
    for (item in items) {
        views.add(toScheduledIdlingManifestView(item))
    }
    return views
}

fun sumScheduledIdlingManifestView(view: ScheduledIdlingManifestView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedMargin + parsedLoadKg + parsedPallets.toDouble()
}
