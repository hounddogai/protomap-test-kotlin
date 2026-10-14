package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event rollup. */
data class CameraEventRollupView(
    var code: String,
    var labelText: String,
    var margin: String,
    var pallets: String,
    var graceDaysText: String,
    var revision: String,
    var visible: String,
    var externalRefText: String,
    var statusLabel: String,
)

fun toCameraEventRollupView(item: CameraEventRollup): CameraEventRollupView {
    return CameraEventRollupView(
        code = item.code,
        labelText = truncate(item.label, 64),
        margin = "%.2f".format(item.margin),
        pallets = item.pallets.toString(),
        graceDaysText = item.graceDays.toString(),
        revision = item.revision.toString(),
        visible = "${item.visible}",
        externalRefText = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventRollupViews(items: MutableList<CameraEventRollup>): MutableList<CameraEventRollupView> {
    val views = mutableListOf<CameraEventRollupView>()
    for (item in items) {
        views.add(toCameraEventRollupView(item))
    }
    return views
}

fun sumCameraEventRollupView(view: CameraEventRollupView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedMargin + parsedPallets.toDouble() + parsedGraceDays.toDouble()
}
