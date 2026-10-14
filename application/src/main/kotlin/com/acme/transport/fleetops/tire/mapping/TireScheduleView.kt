package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire schedule. */
data class TireScheduleView(
    var code: String,
    var label: String,
    var region: String,
    var category: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toTireScheduleView(item: TireSchedule): TireScheduleView {
    return TireScheduleView(
        code = item.code,
        label = truncate(item.label, 64),
        region = item.region,
        category = item.category,
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireScheduleViews(items: MutableList<TireSchedule>): MutableList<TireScheduleView> {
    val views = mutableListOf<TireScheduleView>()
    for (item in items) {
        views.add(toTireScheduleView(item))
    }
    return views
}

fun sumTireScheduleView(view: TireScheduleView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedLoadKg
}
