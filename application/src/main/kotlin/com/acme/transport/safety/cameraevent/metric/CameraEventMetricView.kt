package com.acme.transport.safety.cameraevent.metric

/** How screens and exports show a camera event metric. */
data class CameraEventMetricView(
    var codeText: String,
    var name: String,
    var notesText: String,
    var fuelLiters: String,
    var trailerId: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toCameraEventMetricView(item: CameraEventMetric): CameraEventMetricView {
    return CameraEventMetricView(
        codeText = item.code,
        name = item.name,
        notesText = item.notes,
        fuelLiters = "%.2f".format(item.fuelLiters),
        trailerId = item.trailerId,
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventMetricViews(items: MutableList<CameraEventMetric>): MutableList<CameraEventMetricView> {
    val views = mutableListOf<CameraEventMetricView>()
    for (item in items) {
        views.add(toCameraEventMetricView(item))
    }
    return views
}

fun sumCameraEventMetricView(view: CameraEventMetricView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedFuelLiters
}
