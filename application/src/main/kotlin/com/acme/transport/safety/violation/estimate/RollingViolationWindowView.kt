package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling violation window. */
data class RollingViolationWindowView(
    var code: String,
    var name: String,
    var archived: String,
    var ratioText: String,
    var expiresAt: String,
    var regionText: String,
    var active: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toRollingViolationWindowView(item: RollingViolationWindow): RollingViolationWindowView {
    return RollingViolationWindowView(
        code = item.code,
        name = item.name,
        archived = "${item.archived}",
        ratioText = "%.2f".format(item.ratio),
        expiresAt = item.expiresAt.toString(),
        regionText = truncate(item.region, 40),
        active = "${item.active}",
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingViolationWindowViews(items: MutableList<RollingViolationWindow>): MutableList<RollingViolationWindowView> {
    val views = mutableListOf<RollingViolationWindowView>()
    for (item in items) {
        views.add(toRollingViolationWindowView(item))
    }
    return views
}

fun sumRollingViolationWindowView(view: RollingViolationWindowView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedRatio + parsedExpiresAt.toDouble() + parsedFloorValue
}
