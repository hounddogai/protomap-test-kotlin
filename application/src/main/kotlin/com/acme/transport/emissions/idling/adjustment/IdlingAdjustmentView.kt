package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling adjustment. */
data class IdlingAdjustmentView(
    var codeText: String,
    var label: String,
    var visible: String,
    var durationMinutes: String,
    var locale: String,
    var timezone: String,
    var limitText: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toIdlingAdjustmentView(item: IdlingAdjustment): IdlingAdjustmentView {
    return IdlingAdjustmentView(
        codeText = item.code,
        label = truncate(item.label, 80),
        visible = "${item.visible}",
        durationMinutes = item.durationMinutes.toString(),
        locale = truncate(item.locale, 80),
        timezone = item.timezone,
        limitText = item.limit.toString(),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingAdjustmentViews(items: MutableList<IdlingAdjustment>): MutableList<IdlingAdjustmentView> {
    val views = mutableListOf<IdlingAdjustmentView>()
    for (item in items) {
        views.add(toIdlingAdjustmentView(item))
    }
    return views
}

fun sumIdlingAdjustmentView(view: IdlingAdjustmentView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedDurationMinutes.toDouble() + parsedLimit.toDouble()
}
