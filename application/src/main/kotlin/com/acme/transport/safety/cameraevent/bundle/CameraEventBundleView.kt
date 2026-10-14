package com.acme.transport.safety.cameraevent.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event bundle. */
data class CameraEventBundleView(
    var code: String,
    var title: String,
    var updatedAt: String,
    var floorValue: String,
    var vehicleId: String,
    var discount: String,
    var odometerText: String,
    var reviewerText: String,
    var unitText: String,
    var statusLabel: String,
)

fun toCameraEventBundleView(item: CameraEventBundle): CameraEventBundleView {
    return CameraEventBundleView(
        code = item.code,
        title = item.title,
        updatedAt = item.updatedAt.toString(),
        floorValue = "%.2f".format(item.floorValue),
        vehicleId = truncate(item.vehicleId, 120),
        discount = "%.2f".format(item.discount),
        odometerText = item.odometer.toString(),
        reviewerText = truncate(item.reviewer, 64),
        unitText = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventBundleViews(items: MutableList<CameraEventBundle>): MutableList<CameraEventBundleView> {
    val views = mutableListOf<CameraEventBundleView>()
    for (item in items) {
        views.add(toCameraEventBundleView(item))
    }
    return views
}

fun sumCameraEventBundleView(view: CameraEventBundleView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedUpdatedAt.toDouble() + parsedFloorValue + parsedDiscount
}
