package com.acme.transport.crewing.qualification.qualificationmetric

/** How screens and exports show a pending qualification forecast. */
data class PendingQualificationForecastView(
    var codeText: String,
    var title: String,
    var floorValueText: String,
    var scoreText: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toPendingQualificationForecastView(item: PendingQualificationForecast): PendingQualificationForecastView {
    return PendingQualificationForecastView(
        codeText = item.code,
        title = item.title,
        floorValueText = "%.2f".format(item.floorValue),
        scoreText = "%.2f".format(item.score),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingQualificationForecastViews(items: MutableList<PendingQualificationForecast>): MutableList<PendingQualificationForecastView> {
    val views = mutableListOf<PendingQualificationForecastView>()
    for (item in items) {
        views.add(toPendingQualificationForecastView(item))
    }
    return views
}

fun sumPendingQualificationForecastView(view: PendingQualificationForecastView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedScore = view.scoreText.toDouble()
    return parsedFloorValue + parsedScore
}
