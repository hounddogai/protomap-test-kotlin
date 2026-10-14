package com.acme.transport.safety.nearmiss.plan

/** How screens and exports show a near miss request. */
data class NearMissRequestView(
    var code: String,
    var labelText: String,
    var title: String,
    var lane: String,
    var loadKgText: String,
    var durationMinutes: String,
    var odometer: String,
    var statusLabel: String,
)

fun toNearMissRequestView(item: NearMissRequest): NearMissRequestView {
    return NearMissRequestView(
        code = item.code,
        labelText = item.label,
        title = item.title,
        lane = item.lane,
        loadKgText = "%.2f".format(item.loadKg),
        durationMinutes = item.durationMinutes.toString(),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissRequestViews(items: MutableList<NearMissRequest>): MutableList<NearMissRequestView> {
    val views = mutableListOf<NearMissRequestView>()
    for (item in items) {
        views.add(toNearMissRequestView(item))
    }
    return views
}

fun sumNearMissRequestView(view: NearMissRequestView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedLoadKg + parsedDurationMinutes.toDouble() + parsedOdometer.toDouble()
}
