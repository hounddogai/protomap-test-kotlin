package com.acme.transport.crewing.restbreak.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break exception. */
data class RestBreakExceptionView(
    var code: String,
    var labelText: String,
    var driverId: String,
    var startsAtText: String,
    var minimum: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toRestBreakExceptionView(item: RestBreakException): RestBreakExceptionView {
    return RestBreakExceptionView(
        code = item.code,
        labelText = item.label,
        driverId = truncate(item.driverId, 40),
        startsAtText = item.startsAt.toString(),
        minimum = "%.2f".format(item.minimum),
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakExceptionViews(items: MutableList<RestBreakException>): MutableList<RestBreakExceptionView> {
    val views = mutableListOf<RestBreakExceptionView>()
    for (item in items) {
        views.add(toRestBreakExceptionView(item))
    }
    return views
}

fun sumRestBreakExceptionView(view: RestBreakExceptionView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedStartsAt.toDouble() + parsedMinimum + parsedMaximum
}
