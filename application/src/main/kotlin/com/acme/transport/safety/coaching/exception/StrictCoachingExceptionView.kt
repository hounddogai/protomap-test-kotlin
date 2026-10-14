package com.acme.transport.safety.coaching.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict coaching exception. */
data class StrictCoachingExceptionView(
    var code: String,
    var labelText: String,
    var phone: String,
    var stopCount: String,
    var source: String,
    var locale: String,
    var fuelLitersText: String,
    var notesText: String,
    var statusLabel: String,
)

fun toStrictCoachingExceptionView(item: StrictCoachingException): StrictCoachingExceptionView {
    return StrictCoachingExceptionView(
        code = item.code,
        labelText = item.label,
        phone = item.phone,
        stopCount = item.stopCount.toString(),
        source = truncate(item.source, 64),
        locale = item.locale,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        notesText = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictCoachingExceptionViews(items: MutableList<StrictCoachingException>): MutableList<StrictCoachingExceptionView> {
    val views = mutableListOf<StrictCoachingExceptionView>()
    for (item in items) {
        views.add(toStrictCoachingExceptionView(item))
    }
    return views
}

fun sumStrictCoachingExceptionView(view: StrictCoachingExceptionView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedStopCount.toDouble() + parsedFuelLiters
}
