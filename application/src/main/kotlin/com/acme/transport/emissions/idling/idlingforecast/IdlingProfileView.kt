package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling profile. */
data class IdlingProfileView(
    var code: String,
    var label: String,
    var title: String,
    var distanceKm: String,
    var ownerId: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toIdlingProfileView(item: IdlingProfile): IdlingProfileView {
    return IdlingProfileView(
        code = truncate(item.code, 120),
        label = item.label,
        title = truncate(item.title, 120),
        distanceKm = "%.2f".format(item.distanceKm),
        ownerId = truncate(item.ownerId, 120),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingProfileViews(items: MutableList<IdlingProfile>): MutableList<IdlingProfileView> {
    val views = mutableListOf<IdlingProfileView>()
    for (item in items) {
        views.add(toIdlingProfileView(item))
    }
    return views
}

fun sumIdlingProfileView(view: IdlingProfileView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedDistanceKm + parsedCeiling
}
