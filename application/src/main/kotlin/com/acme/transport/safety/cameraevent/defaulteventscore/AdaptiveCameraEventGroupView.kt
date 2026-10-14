package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive camera event group. */
data class AdaptiveCameraEventGroupView(
    var code: String,
    var title: String,
    var durationMinutes: String,
    var fuelLiters: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toAdaptiveCameraEventGroupView(item: AdaptiveCameraEventGroup): AdaptiveCameraEventGroupView {
    return AdaptiveCameraEventGroupView(
        code = truncate(item.code, 64),
        title = item.title,
        durationMinutes = item.durationMinutes.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveCameraEventGroupViews(items: MutableList<AdaptiveCameraEventGroup>): MutableList<AdaptiveCameraEventGroupView> {
    val views = mutableListOf<AdaptiveCameraEventGroupView>()
    for (item in items) {
        views.add(toAdaptiveCameraEventGroupView(item))
    }
    return views
}

fun sumAdaptiveCameraEventGroupView(view: AdaptiveCameraEventGroupView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedFuelLiters + parsedRatio
}
