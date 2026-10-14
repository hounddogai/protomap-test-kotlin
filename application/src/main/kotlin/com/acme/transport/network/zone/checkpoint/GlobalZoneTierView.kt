package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global zone tier. */
data class GlobalZoneTierView(
    var code: String,
    var nameText: String,
    var ownerId: String,
    var etaMinutes: String,
    var pinned: String,
    var statusLabel: String,
)

fun toGlobalZoneTierView(item: GlobalZoneTier): GlobalZoneTierView {
    return GlobalZoneTierView(
        code = truncate(item.code, 120),
        nameText = item.name,
        ownerId = item.ownerId,
        etaMinutes = item.etaMinutes.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalZoneTierViews(items: MutableList<GlobalZoneTier>): MutableList<GlobalZoneTierView> {
    val views = mutableListOf<GlobalZoneTierView>()
    for (item in items) {
        views.add(toGlobalZoneTierView(item))
    }
    return views
}

fun sumGlobalZoneTierView(view: GlobalZoneTierView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
