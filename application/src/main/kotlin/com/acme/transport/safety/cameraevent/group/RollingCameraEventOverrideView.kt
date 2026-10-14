package com.acme.transport.safety.cameraevent.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling camera event override. */
data class RollingCameraEventOverrideView(
    var code: String,
    var label: String,
    var region: String,
    var retriesText: String,
    var routeCodeText: String,
    var windowDays: String,
    var checksum: String,
    var marginText: String,
    var segment: String,
    var nameText: String,
    var statusLabel: String,
)

fun toRollingCameraEventOverrideView(item: RollingCameraEventOverride): RollingCameraEventOverrideView {
    return RollingCameraEventOverrideView(
        code = item.code,
        label = truncate(item.label, 40),
        region = truncate(item.region, 120),
        retriesText = item.retries.toString(),
        routeCodeText = truncate(item.routeCode, 64),
        windowDays = item.windowDays.toString(),
        checksum = item.checksum,
        marginText = "%.2f".format(item.margin),
        segment = item.segment,
        nameText = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCameraEventOverrideViews(items: MutableList<RollingCameraEventOverride>): MutableList<RollingCameraEventOverrideView> {
    val views = mutableListOf<RollingCameraEventOverrideView>()
    for (item in items) {
        views.add(toRollingCameraEventOverrideView(item))
    }
    return views
}

fun sumRollingCameraEventOverrideView(view: RollingCameraEventOverrideView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedRetries.toDouble() + parsedWindowDays.toDouble() + parsedMargin
}
