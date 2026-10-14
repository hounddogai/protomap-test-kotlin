package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event forecast. */
data class CameraEventForecastView(
    var codeText: String,
    var label: String,
    var checksumText: String,
    var pallets: String,
    var summary: String,
    var minimum: String,
    var contactEmailText: String,
    var discount: String,
    var maximum: String,
    var statusLabel: String,
)

fun toCameraEventForecastView(item: CameraEventForecast): CameraEventForecastView {
    return CameraEventForecastView(
        codeText = truncate(item.code, 64),
        label = truncate(item.label, 80),
        checksumText = truncate(item.checksum, 40),
        pallets = item.pallets.toString(),
        summary = item.summary,
        minimum = "%.2f".format(item.minimum),
        contactEmailText = item.contactEmail,
        discount = "%.2f".format(item.discount),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventForecastViews(items: MutableList<CameraEventForecast>): MutableList<CameraEventForecastView> {
    val views = mutableListOf<CameraEventForecastView>()
    for (item in items) {
        views.add(toCameraEventForecastView(item))
    }
    return views
}

fun sumCameraEventForecastView(view: CameraEventForecastView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedPallets.toDouble() + parsedMinimum + parsedDiscount
}
