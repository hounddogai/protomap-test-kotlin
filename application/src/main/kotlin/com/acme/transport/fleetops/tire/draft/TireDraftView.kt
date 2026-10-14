package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire draft. */
data class TireDraftView(
    var code: String,
    var label: String,
    var expiresAt: String,
    var unit: String,
    var limit: String,
    var rate: String,
    var depotText: String,
    var createdAt: String,
    var checksum: String,
    var statusLabel: String,
)

fun toTireDraftView(item: TireDraft): TireDraftView {
    return TireDraftView(
        code = item.code,
        label = item.label,
        expiresAt = item.expiresAt.toString(),
        unit = item.unit,
        limit = item.limit.toString(),
        rate = "%.2f".format(item.rate),
        depotText = truncate(item.depot, 80),
        createdAt = item.createdAt.toString(),
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireDraftViews(items: MutableList<TireDraft>): MutableList<TireDraftView> {
    val views = mutableListOf<TireDraftView>()
    for (item in items) {
        views.add(toTireDraftView(item))
    }
    return views
}

fun sumTireDraftView(view: TireDraftView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedExpiresAt.toDouble() + parsedLimit.toDouble() + parsedRate
}
