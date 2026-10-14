package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity record. */
data class CapacityRecordView(
    var code: String,
    var name: String,
    var vehicleId: String,
    var minimum: String,
    var source: String,
    var statusLabel: String,
)

fun toCapacityRecordView(item: CapacityRecord): CapacityRecordView {
    return CapacityRecordView(
        code = item.code,
        name = truncate(item.name, 80),
        vehicleId = item.vehicleId,
        minimum = "%.2f".format(item.minimum),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityRecordViews(items: MutableList<CapacityRecord>): MutableList<CapacityRecordView> {
    val views = mutableListOf<CapacityRecordView>()
    for (item in items) {
        views.add(toCapacityRecordView(item))
    }
    return views
}

fun sumCapacityRecordView(view: CapacityRecordView): Double {
    val parsedMinimum = view.minimum.toDouble()
    return parsedMinimum
}
