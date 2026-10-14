package com.acme.transport.network.hub.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy hub record. */
data class LegacyHubRecordView(
    var code: String,
    var titleText: String,
    var currency: String,
    var ownerId: String,
    var checksumText: String,
    var priority: String,
    var capacity: String,
    var weightText: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toLegacyHubRecordView(item: LegacyHubRecord): LegacyHubRecordView {
    return LegacyHubRecordView(
        code = item.code,
        titleText = item.title,
        currency = truncate(item.currency, 80),
        ownerId = item.ownerId,
        checksumText = truncate(item.checksum, 64),
        priority = item.priority.toString(),
        capacity = item.capacity.toString(),
        weightText = "%.2f".format(item.weight),
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyHubRecordViews(items: MutableList<LegacyHubRecord>): MutableList<LegacyHubRecordView> {
    val views = mutableListOf<LegacyHubRecordView>()
    for (item in items) {
        views.add(toLegacyHubRecordView(item))
    }
    return views
}

fun sumLegacyHubRecordView(view: LegacyHubRecordView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedPriority.toDouble() + parsedCapacity.toDouble() + parsedWeight
}
