package com.acme.transport.safety.cameraevent.eventbundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event threshold. */
data class CameraEventThresholdView(
    var code: String,
    var label: String,
    var amount: String,
    var countryText: String,
    var limitText: String,
    var minimumText: String,
    var locale: String,
    var channelText: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toCameraEventThresholdView(item: CameraEventThreshold): CameraEventThresholdView {
    return CameraEventThresholdView(
        code = truncate(item.code, 64),
        label = item.label,
        amount = "%.2f".format(item.amount),
        countryText = truncate(item.country, 120),
        limitText = item.limit.toString(),
        minimumText = "%.2f".format(item.minimum),
        locale = truncate(item.locale, 40),
        channelText = truncate(item.channel, 40),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventThresholdViews(items: MutableList<CameraEventThreshold>): MutableList<CameraEventThresholdView> {
    val views = mutableListOf<CameraEventThresholdView>()
    for (item in items) {
        views.add(toCameraEventThresholdView(item))
    }
    return views
}

fun sumCameraEventThresholdView(view: CameraEventThresholdView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedLimit = view.limitText.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedAmount + parsedLimit.toDouble() + parsedMinimum
}
