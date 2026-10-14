package com.acme.transport.customs.clearance.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling clearance adjustment. */
data class RollingClearanceAdjustmentView(
    var codeText: String,
    var title: String,
    var label: String,
    var minimum: String,
    var retries: String,
    var lane: String,
    var pinned: String,
    var floorValueText: String,
    var localeText: String,
    var statusLabel: String,
)

fun toRollingClearanceAdjustmentView(item: RollingClearanceAdjustment): RollingClearanceAdjustmentView {
    return RollingClearanceAdjustmentView(
        codeText = item.code,
        title = truncate(item.title, 120),
        label = item.label,
        minimum = "%.2f".format(item.minimum),
        retries = item.retries.toString(),
        lane = item.lane,
        pinned = "${item.pinned}",
        floorValueText = "%.2f".format(item.floorValue),
        localeText = truncate(item.locale, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingClearanceAdjustmentViews(items: MutableList<RollingClearanceAdjustment>): MutableList<RollingClearanceAdjustmentView> {
    val views = mutableListOf<RollingClearanceAdjustmentView>()
    for (item in items) {
        views.add(toRollingClearanceAdjustmentView(item))
    }
    return views
}

fun sumRollingClearanceAdjustmentView(view: RollingClearanceAdjustmentView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedMinimum + parsedRetries.toDouble() + parsedFloorValue
}
