package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event estimate. */
data class CameraEventEstimateView(
    var codeText: String,
    var name: String,
    var externalRef: String,
    var summary: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toCameraEventEstimateView(item: CameraEventEstimate): CameraEventEstimateView {
    return CameraEventEstimateView(
        codeText = truncate(item.code, 64),
        name = item.name,
        externalRef = truncate(item.externalRef, 80),
        summary = item.summary,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventEstimateViews(items: MutableList<CameraEventEstimate>): MutableList<CameraEventEstimateView> {
    val views = mutableListOf<CameraEventEstimateView>()
    for (item in items) {
        views.add(toCameraEventEstimateView(item))
    }
    return views
}

fun sumCameraEventEstimateView(view: CameraEventEstimateView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
