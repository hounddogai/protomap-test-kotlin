package com.acme.transport.customs.tariff.profile

/** How screens and exports show a tariff baseline. */
data class TariffBaselineView(
    var code: String,
    var title: String,
    var attemptsText: String,
    var maximum: String,
    var vehicleId: String,
    var score: String,
    var notesText: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toTariffBaselineView(item: TariffBaseline): TariffBaselineView {
    return TariffBaselineView(
        code = item.code,
        title = item.title,
        attemptsText = item.attempts.toString(),
        maximum = "%.2f".format(item.maximum),
        vehicleId = item.vehicleId,
        score = "%.2f".format(item.score),
        notesText = item.notes,
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffBaselineViews(items: MutableList<TariffBaseline>): MutableList<TariffBaselineView> {
    val views = mutableListOf<TariffBaselineView>()
    for (item in items) {
        views.add(toTariffBaselineView(item))
    }
    return views
}

fun sumTariffBaselineView(view: TariffBaselineView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedAttempts.toDouble() + parsedMaximum + parsedScore
}
