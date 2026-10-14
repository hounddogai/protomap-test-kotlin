package com.acme.transport.network.zone.pendingzonemapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending zone mapping. */
data class PendingZoneMappingView(
    var code: String,
    var labelText: String,
    var trailerId: String,
    var updatedAt: String,
    var startsAtText: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toPendingZoneMappingView(item: PendingZoneMapping): PendingZoneMappingView {
    return PendingZoneMappingView(
        code = truncate(item.code, 64),
        labelText = item.label,
        trailerId = item.trailerId,
        updatedAt = item.updatedAt.toString(),
        startsAtText = item.startsAt.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingZoneMappingViews(items: MutableList<PendingZoneMapping>): MutableList<PendingZoneMappingView> {
    val views = mutableListOf<PendingZoneMappingView>()
    for (item in items) {
        views.add(toPendingZoneMappingView(item))
    }
    return views
}

fun sumPendingZoneMappingView(view: PendingZoneMappingView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedUpdatedAt.toDouble() + parsedStartsAt.toDouble() + parsedLoadKg
}
