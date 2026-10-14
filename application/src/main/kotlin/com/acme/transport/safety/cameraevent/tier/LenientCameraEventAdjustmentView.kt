package com.acme.transport.safety.cameraevent.tier

/** How screens and exports show a lenient camera event adjustment. */
data class LenientCameraEventAdjustmentView(
    var codeText: String,
    var label: String,
    var unit: String,
    var trailerId: String,
    var version: String,
    var statusLabel: String,
)

fun toLenientCameraEventAdjustmentView(item: LenientCameraEventAdjustment): LenientCameraEventAdjustmentView {
    return LenientCameraEventAdjustmentView(
        codeText = item.code,
        label = item.label,
        unit = item.unit,
        trailerId = item.trailerId,
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientCameraEventAdjustmentViews(items: MutableList<LenientCameraEventAdjustment>): MutableList<LenientCameraEventAdjustmentView> {
    val views = mutableListOf<LenientCameraEventAdjustmentView>()
    for (item in items) {
        views.add(toLenientCameraEventAdjustmentView(item))
    }
    return views
}

fun sumLenientCameraEventAdjustmentView(view: LenientCameraEventAdjustmentView): Double {
    val parsedVersion = view.version.toInt()
    return parsedVersion.toDouble()
}
