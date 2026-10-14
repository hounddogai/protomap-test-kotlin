package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event policy. */
data class CameraEventPolicyView(
    var code: String,
    var nameText: String,
    var vehicleIdText: String,
    var label: String,
    var timezoneText: String,
    var quantityText: String,
    var discount: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toCameraEventPolicyView(item: CameraEventPolicy): CameraEventPolicyView {
    return CameraEventPolicyView(
        code = truncate(item.code, 80),
        nameText = item.name,
        vehicleIdText = item.vehicleId,
        label = truncate(item.label, 120),
        timezoneText = item.timezone,
        quantityText = item.quantity.toString(),
        discount = "%.2f".format(item.discount),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventPolicyViews(items: MutableList<CameraEventPolicy>): MutableList<CameraEventPolicyView> {
    val views = mutableListOf<CameraEventPolicyView>()
    for (item in items) {
        views.add(toCameraEventPolicyView(item))
    }
    return views
}

fun sumCameraEventPolicyView(view: CameraEventPolicyView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedQuantity.toDouble() + parsedDiscount + parsedEndsAt.toDouble()
}
