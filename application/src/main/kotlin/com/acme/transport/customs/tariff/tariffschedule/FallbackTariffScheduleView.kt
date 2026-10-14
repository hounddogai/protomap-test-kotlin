package com.acme.transport.customs.tariff.tariffschedule

/** How screens and exports show a fallback tariff schedule. */
data class FallbackTariffScheduleView(
    var code: String,
    var nameText: String,
    var score: String,
    var trailerId: String,
    var visible: String,
    var limit: String,
    var statusLabel: String,
)

fun toFallbackTariffScheduleView(item: FallbackTariffSchedule): FallbackTariffScheduleView {
    return FallbackTariffScheduleView(
        code = item.code,
        nameText = item.name,
        score = "%.2f".format(item.score),
        trailerId = item.trailerId,
        visible = "${item.visible}",
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackTariffScheduleViews(items: MutableList<FallbackTariffSchedule>): MutableList<FallbackTariffScheduleView> {
    val views = mutableListOf<FallbackTariffScheduleView>()
    for (item in items) {
        views.add(toFallbackTariffScheduleView(item))
    }
    return views
}

fun sumFallbackTariffScheduleView(view: FallbackTariffScheduleView): Double {
    val parsedScore = view.score.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedScore + parsedLimit.toDouble()
}
