package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event summary. */
data class CameraEventSummaryView(
    var code: String,
    var title: String,
    var region: String,
    var odometer: String,
    var locale: String,
    var margin: String,
    var channelText: String,
    var quantityText: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toCameraEventSummaryView(item: CameraEventSummary): CameraEventSummaryView {
    return CameraEventSummaryView(
        code = truncate(item.code, 120),
        title = item.title,
        region = item.region,
        odometer = item.odometer.toString(),
        locale = item.locale,
        margin = "%.2f".format(item.margin),
        channelText = item.channel,
        quantityText = item.quantity.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventSummaryViews(items: MutableList<CameraEventSummary>): MutableList<CameraEventSummaryView> {
    val views = mutableListOf<CameraEventSummaryView>()
    for (item in items) {
        views.add(toCameraEventSummaryView(item))
    }
    return views
}

fun sumCameraEventSummaryView(view: CameraEventSummaryView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedOdometer.toDouble() + parsedMargin + parsedQuantity.toDouble()
}
