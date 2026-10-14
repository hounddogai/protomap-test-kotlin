package com.acme.transport.linehaul.capacity.rollup

/** How screens and exports show a capacity rollup. */
data class CapacityRollupView(
    var code: String,
    var label: String,
    var threshold: String,
    var updatedAt: String,
    var active: String,
    var minimumText: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toCapacityRollupView(item: CapacityRollup): CapacityRollupView {
    return CapacityRollupView(
        code = item.code,
        label = item.label,
        threshold = "%.2f".format(item.threshold),
        updatedAt = item.updatedAt.toString(),
        active = "${item.active}",
        minimumText = "%.2f".format(item.minimum),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityRollupViews(items: MutableList<CapacityRollup>): MutableList<CapacityRollupView> {
    val views = mutableListOf<CapacityRollupView>()
    for (item in items) {
        views.add(toCapacityRollupView(item))
    }
    return views
}

fun sumCapacityRollupView(view: CapacityRollupView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedThreshold + parsedUpdatedAt.toDouble() + parsedMinimum
}
