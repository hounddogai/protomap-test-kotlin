package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial batch. */
data class AccessorialBatchView(
    var codeText: String,
    var label: String,
    var floorValue: String,
    var odometer: String,
    var statusLabel: String,
)

fun toAccessorialBatchView(item: AccessorialBatch): AccessorialBatchView {
    return AccessorialBatchView(
        codeText = item.code,
        label = truncate(item.label, 120),
        floorValue = "%.2f".format(item.floorValue),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialBatchViews(items: MutableList<AccessorialBatch>): MutableList<AccessorialBatchView> {
    val views = mutableListOf<AccessorialBatchView>()
    for (item in items) {
        views.add(toAccessorialBatchView(item))
    }
    return views
}

fun sumAccessorialBatchView(view: AccessorialBatchView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedFloorValue + parsedOdometer.toDouble()
}
