package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual trailer template. */
data class ManualTrailerTemplateView(
    var code: String,
    var title: String,
    var weight: String,
    var endsAt: String,
    var windowDaysText: String,
    var fuelLiters: String,
    var laneText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toManualTrailerTemplateView(item: ManualTrailerTemplate): ManualTrailerTemplateView {
    return ManualTrailerTemplateView(
        code = truncate(item.code, 120),
        title = item.title,
        weight = "%.2f".format(item.weight),
        endsAt = item.endsAt.toString(),
        windowDaysText = item.windowDays.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        laneText = item.lane,
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTrailerTemplateViews(items: MutableList<ManualTrailerTemplate>): MutableList<ManualTrailerTemplateView> {
    val views = mutableListOf<ManualTrailerTemplateView>()
    for (item in items) {
        views.add(toManualTrailerTemplateView(item))
    }
    return views
}

fun sumManualTrailerTemplateView(view: ManualTrailerTemplateView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedWeight + parsedEndsAt.toDouble() + parsedWindowDays.toDouble()
}
