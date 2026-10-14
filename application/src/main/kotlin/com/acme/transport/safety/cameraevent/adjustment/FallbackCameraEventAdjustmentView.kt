package com.acme.transport.safety.cameraevent.adjustment

/** How screens and exports show a fallback camera event adjustment. */
data class FallbackCameraEventAdjustmentView(
    var codeText: String,
    var label: String,
    var distanceKmText: String,
    var approved: String,
    var discount: String,
    var visible: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toFallbackCameraEventAdjustmentView(item: FallbackCameraEventAdjustment): FallbackCameraEventAdjustmentView {
    return FallbackCameraEventAdjustmentView(
        codeText = item.code,
        label = item.label,
        distanceKmText = "%.2f".format(item.distanceKm),
        approved = "${item.approved}",
        discount = "%.2f".format(item.discount),
        visible = "${item.visible}",
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCameraEventAdjustmentViews(items: MutableList<FallbackCameraEventAdjustment>): MutableList<FallbackCameraEventAdjustmentView> {
    val views = mutableListOf<FallbackCameraEventAdjustmentView>()
    for (item in items) {
        views.add(toFallbackCameraEventAdjustmentView(item))
    }
    return views
}

fun sumFallbackCameraEventAdjustmentView(view: FallbackCameraEventAdjustmentView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedDistanceKm + parsedDiscount + parsedCeiling
}
