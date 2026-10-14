package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity bundle. */
data class CapacityBundleView(
    var code: String,
    var title: String,
    var priority: String,
    var graceDays: String,
    var sourceText: String,
    var externalRef: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toCapacityBundleView(item: CapacityBundle): CapacityBundleView {
    return CapacityBundleView(
        code = item.code,
        title = item.title,
        priority = item.priority.toString(),
        graceDays = item.graceDays.toString(),
        sourceText = item.source,
        externalRef = truncate(item.externalRef, 40),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityBundleViews(items: MutableList<CapacityBundle>): MutableList<CapacityBundleView> {
    val views = mutableListOf<CapacityBundleView>()
    for (item in items) {
        views.add(toCapacityBundleView(item))
    }
    return views
}

fun sumCapacityBundleView(view: CapacityBundleView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedPriority.toDouble() + parsedGraceDays.toDouble() + parsedRatio
}
