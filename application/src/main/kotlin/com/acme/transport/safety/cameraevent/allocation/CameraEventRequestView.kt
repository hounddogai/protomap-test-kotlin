package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event request. */
data class CameraEventRequestView(
    var code: String,
    var labelText: String,
    var approved: String,
    var amountText: String,
    var dock: String,
    var pallets: String,
    var costText: String,
    var statusLabel: String,
)

fun toCameraEventRequestView(item: CameraEventRequest): CameraEventRequestView {
    return CameraEventRequestView(
        code = item.code,
        labelText = item.label,
        approved = "${item.approved}",
        amountText = "%.2f".format(item.amount),
        dock = truncate(item.dock, 64),
        pallets = item.pallets.toString(),
        costText = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventRequestViews(items: MutableList<CameraEventRequest>): MutableList<CameraEventRequestView> {
    val views = mutableListOf<CameraEventRequestView>()
    for (item in items) {
        views.add(toCameraEventRequestView(item))
    }
    return views
}

fun sumCameraEventRequestView(view: CameraEventRequestView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedAmount + parsedPallets.toDouble() + parsedCost
}
