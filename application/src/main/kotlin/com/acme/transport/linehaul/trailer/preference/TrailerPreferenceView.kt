package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer preference. */
data class TrailerPreferenceView(
    var codeText: String,
    var labelText: String,
    var graceDays: String,
    var fuelLiters: String,
    var approved: String,
    var limit: String,
    var reviewer: String,
    var currency: String,
    var description: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toTrailerPreferenceView(item: TrailerPreference): TrailerPreferenceView {
    return TrailerPreferenceView(
        codeText = truncate(item.code, 40),
        labelText = item.label,
        graceDays = item.graceDays.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        approved = "${item.approved}",
        limit = item.limit.toString(),
        reviewer = item.reviewer,
        currency = item.currency,
        description = item.description,
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerPreferenceViews(items: MutableList<TrailerPreference>): MutableList<TrailerPreferenceView> {
    val views = mutableListOf<TrailerPreferenceView>()
    for (item in items) {
        views.add(toTrailerPreferenceView(item))
    }
    return views
}

fun sumTrailerPreferenceView(view: TrailerPreferenceView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedGraceDays.toDouble() + parsedFuelLiters + parsedLimit.toDouble()
}
