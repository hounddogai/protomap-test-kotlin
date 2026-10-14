package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event allocation. */
data class CameraEventAllocationView(
    var code: String,
    var name: String,
    var fuelLitersText: String,
    var priority: String,
    var ceiling: String,
    var phone: String,
    var etaMinutesText: String,
    var contactEmailText: String,
    var discount: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toCameraEventAllocationView(item: CameraEventAllocation): CameraEventAllocationView {
    return CameraEventAllocationView(
        code = item.code,
        name = item.name,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        priority = item.priority.toString(),
        ceiling = "%.2f".format(item.ceiling),
        phone = truncate(item.phone, 80),
        etaMinutesText = item.etaMinutes.toString(),
        contactEmailText = item.contactEmail,
        discount = "%.2f".format(item.discount),
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventAllocationViews(items: MutableList<CameraEventAllocation>): MutableList<CameraEventAllocationView> {
    val views = mutableListOf<CameraEventAllocationView>()
    for (item in items) {
        views.add(toCameraEventAllocationView(item))
    }
    return views
}

fun sumCameraEventAllocationView(view: CameraEventAllocationView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedFuelLiters + parsedPriority.toDouble() + parsedCeiling
}
