package com.acme.transport.customs.tariff.tariffagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict tariff score. */
data class StrictTariffScoreView(
    var code: String,
    var title: String,
    var channel: String,
    var approved: String,
    var active: String,
    var label: String,
    var laneText: String,
    var notes: String,
    var phone: String,
    var country: String,
    var score: String,
    var statusLabel: String,
)

fun toStrictTariffScoreView(item: StrictTariffScore): StrictTariffScoreView {
    return StrictTariffScoreView(
        code = truncate(item.code, 80),
        title = truncate(item.title, 64),
        channel = item.channel,
        approved = "${item.approved}",
        active = "${item.active}",
        label = item.label,
        laneText = item.lane,
        notes = item.notes,
        phone = truncate(item.phone, 40),
        country = item.country,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTariffScoreViews(items: MutableList<StrictTariffScore>): MutableList<StrictTariffScoreView> {
    val views = mutableListOf<StrictTariffScoreView>()
    for (item in items) {
        views.add(toStrictTariffScoreView(item))
    }
    return views
}

fun sumStrictTariffScoreView(view: StrictTariffScoreView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
