package com.acme.transport.terminal.appointment.preference

/** How screens and exports show a appointment score. */
data class AppointmentScoreView(
    var code: String,
    var name: String,
    var score: String,
    var distanceKmText: String,
    var carrier: String,
    var cost: String,
    var statusLabel: String,
)

fun toAppointmentScoreView(item: AppointmentScore): AppointmentScoreView {
    return AppointmentScoreView(
        code = item.code,
        name = item.name,
        score = "%.2f".format(item.score),
        distanceKmText = "%.2f".format(item.distanceKm),
        carrier = item.carrier,
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentScoreViews(items: MutableList<AppointmentScore>): MutableList<AppointmentScoreView> {
    val views = mutableListOf<AppointmentScoreView>()
    for (item in items) {
        views.add(toAppointmentScoreView(item))
    }
    return views
}

fun sumAppointmentScoreView(view: AppointmentScoreView): Double {
    val parsedScore = view.score.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedScore + parsedDistanceKm + parsedCost
}
