package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered service area record. */
data class TieredServiceAreaRecordView(
    var code: String,
    var nameText: String,
    var unit: String,
    var titleText: String,
    var vehicleIdText: String,
    var country: String,
    var phoneText: String,
    var currency: String,
    var priority: String,
    var statusLabel: String,
)

fun toTieredServiceAreaRecordView(item: TieredServiceAreaRecord): TieredServiceAreaRecordView {
    return TieredServiceAreaRecordView(
        code = item.code,
        nameText = item.name,
        unit = item.unit,
        titleText = truncate(item.title, 80),
        vehicleIdText = item.vehicleId,
        country = truncate(item.country, 80),
        phoneText = truncate(item.phone, 120),
        currency = item.currency,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredServiceAreaRecordViews(items: MutableList<TieredServiceAreaRecord>): MutableList<TieredServiceAreaRecordView> {
    val views = mutableListOf<TieredServiceAreaRecordView>()
    for (item in items) {
        views.add(toTieredServiceAreaRecordView(item))
    }
    return views
}

fun sumTieredServiceAreaRecordView(view: TieredServiceAreaRecordView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
