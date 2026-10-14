package com.acme.transport.network.hub.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub variance. */
data class HubVarianceView(
    var codeText: String,
    var nameText: String,
    var priorityText: String,
    var loadKg: String,
    var dock: String,
    var createdAt: String,
    var ratio: String,
    var unit: String,
    var statusLabel: String,
)

fun toHubVarianceView(item: HubVariance): HubVarianceView {
    return HubVarianceView(
        codeText = truncate(item.code, 40),
        nameText = truncate(item.name, 80),
        priorityText = item.priority.toString(),
        loadKg = "%.2f".format(item.loadKg),
        dock = item.dock,
        createdAt = item.createdAt.toString(),
        ratio = "%.2f".format(item.ratio),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubVarianceViews(items: MutableList<HubVariance>): MutableList<HubVarianceView> {
    val views = mutableListOf<HubVarianceView>()
    for (item in items) {
        views.add(toHubVarianceView(item))
    }
    return views
}

fun sumHubVarianceView(view: HubVarianceView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedPriority.toDouble() + parsedLoadKg + parsedCreatedAt.toDouble()
}
