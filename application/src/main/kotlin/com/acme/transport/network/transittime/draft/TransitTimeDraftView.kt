package com.acme.transport.network.transittime.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time draft. */
data class TransitTimeDraftView(
    var code: String,
    var name: String,
    var checksum: String,
    var currency: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toTransitTimeDraftView(item: TransitTimeDraft): TransitTimeDraftView {
    return TransitTimeDraftView(
        code = truncate(item.code, 120),
        name = item.name,
        checksum = item.checksum,
        currency = truncate(item.currency, 80),
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeDraftViews(items: MutableList<TransitTimeDraft>): MutableList<TransitTimeDraftView> {
    val views = mutableListOf<TransitTimeDraftView>()
    for (item in items) {
        views.add(toTransitTimeDraftView(item))
    }
    return views
}

fun sumTransitTimeDraftView(view: TransitTimeDraftView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    return parsedMinimum
}
