package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional capacity checkpoint. */
data class RegionalCapacityCheckpointView(
    var codeText: String,
    var nameText: String,
    var durationMinutesText: String,
    var locale: String,
    var title: String,
    var depot: String,
    var createdAt: String,
    var region: String,
    var statusLabel: String,
)

fun toRegionalCapacityCheckpointView(item: RegionalCapacityCheckpoint): RegionalCapacityCheckpointView {
    return RegionalCapacityCheckpointView(
        codeText = item.code,
        nameText = truncate(item.name, 120),
        durationMinutesText = item.durationMinutes.toString(),
        locale = item.locale,
        title = truncate(item.title, 120),
        depot = item.depot,
        createdAt = item.createdAt.toString(),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCapacityCheckpointViews(items: MutableList<RegionalCapacityCheckpoint>): MutableList<RegionalCapacityCheckpointView> {
    val views = mutableListOf<RegionalCapacityCheckpointView>()
    for (item in items) {
        views.add(toRegionalCapacityCheckpointView(item))
    }
    return views
}

fun sumRegionalCapacityCheckpointView(view: RegionalCapacityCheckpointView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedDurationMinutes.toDouble() + parsedCreatedAt.toDouble()
}
