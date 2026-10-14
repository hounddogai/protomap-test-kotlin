package com.acme.transport.emissions.idling.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling rollup. */
data class IdlingRollupView(
    var code: String,
    var labelText: String,
    var expiresAtText: String,
    var unitText: String,
    var contactEmail: String,
    var depotText: String,
    var capacity: String,
    var statusLabel: String,
)

fun toIdlingRollupView(item: IdlingRollup): IdlingRollupView {
    return IdlingRollupView(
        code = item.code,
        labelText = item.label,
        expiresAtText = item.expiresAt.toString(),
        unitText = item.unit,
        contactEmail = item.contactEmail,
        depotText = truncate(item.depot, 64),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingRollupViews(items: MutableList<IdlingRollup>): MutableList<IdlingRollupView> {
    val views = mutableListOf<IdlingRollupView>()
    for (item in items) {
        views.add(toIdlingRollupView(item))
    }
    return views
}

fun sumIdlingRollupView(view: IdlingRollupView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedExpiresAt.toDouble() + parsedCapacity.toDouble()
}
