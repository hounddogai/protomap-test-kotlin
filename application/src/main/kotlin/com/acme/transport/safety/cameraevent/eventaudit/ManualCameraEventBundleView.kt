package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual camera event bundle. */
data class ManualCameraEventBundleView(
    var codeText: String,
    var nameText: String,
    var dock: String,
    var driverId: String,
    var summaryText: String,
    var category: String,
    var visibleText: String,
    var amountText: String,
    var statusLabel: String,
)

fun toManualCameraEventBundleView(item: ManualCameraEventBundle): ManualCameraEventBundleView {
    return ManualCameraEventBundleView(
        codeText = item.code,
        nameText = item.name,
        dock = truncate(item.dock, 80),
        driverId = item.driverId,
        summaryText = item.summary,
        category = item.category,
        visibleText = "${item.visible}",
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCameraEventBundleViews(items: MutableList<ManualCameraEventBundle>): MutableList<ManualCameraEventBundleView> {
    val views = mutableListOf<ManualCameraEventBundleView>()
    for (item in items) {
        views.add(toManualCameraEventBundleView(item))
    }
    return views
}

fun sumManualCameraEventBundleView(view: ManualCameraEventBundleView): Double {
    val parsedAmount = view.amountText.toDouble()
    return parsedAmount
}
