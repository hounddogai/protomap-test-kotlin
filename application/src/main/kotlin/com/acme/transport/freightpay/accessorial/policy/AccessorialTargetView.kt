package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial target. */
data class AccessorialTargetView(
    var code: String,
    var label: String,
    var updatedAt: String,
    var timezone: String,
    var dock: String,
    var description: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toAccessorialTargetView(item: AccessorialTarget): AccessorialTargetView {
    return AccessorialTargetView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 64),
        updatedAt = item.updatedAt.toString(),
        timezone = item.timezone,
        dock = truncate(item.dock, 64),
        description = truncate(item.description, 120),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialTargetViews(items: MutableList<AccessorialTarget>): MutableList<AccessorialTargetView> {
    val views = mutableListOf<AccessorialTargetView>()
    for (item in items) {
        views.add(toAccessorialTargetView(item))
    }
    return views
}

fun sumAccessorialTargetView(view: AccessorialTargetView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedUpdatedAt.toDouble() + parsedDistanceKm
}
