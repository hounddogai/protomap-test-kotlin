package com.acme.transport.crewing.availability.request

/** How screens and exports show a availability request. */
data class AvailabilityRequestView(
    var code: String,
    var labelText: String,
    var active: String,
    var stopCountText: String,
    var weightText: String,
    var statusLabel: String,
)

fun toAvailabilityRequestView(item: AvailabilityRequest): AvailabilityRequestView {
    return AvailabilityRequestView(
        code = item.code,
        labelText = item.label,
        active = "${item.active}",
        stopCountText = item.stopCount.toString(),
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityRequestViews(items: MutableList<AvailabilityRequest>): MutableList<AvailabilityRequestView> {
    val views = mutableListOf<AvailabilityRequestView>()
    for (item in items) {
        views.add(toAvailabilityRequestView(item))
    }
    return views
}

fun sumAvailabilityRequestView(view: AvailabilityRequestView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedStopCount.toDouble() + parsedWeight
}
