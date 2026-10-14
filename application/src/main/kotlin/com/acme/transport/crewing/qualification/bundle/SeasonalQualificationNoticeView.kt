package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal qualification notice. */
data class SeasonalQualificationNoticeView(
    var code: String,
    var label: String,
    var odometer: String,
    var region: String,
    var sequence: String,
    var ratio: String,
    var createdAtText: String,
    var reviewer: String,
    var score: String,
    var margin: String,
    var amount: String,
    var statusLabel: String,
)

fun toSeasonalQualificationNoticeView(item: SeasonalQualificationNotice): SeasonalQualificationNoticeView {
    return SeasonalQualificationNoticeView(
        code = item.code,
        label = item.label,
        odometer = item.odometer.toString(),
        region = item.region,
        sequence = item.sequence.toString(),
        ratio = "%.2f".format(item.ratio),
        createdAtText = item.createdAt.toString(),
        reviewer = truncate(item.reviewer, 120),
        score = "%.2f".format(item.score),
        margin = "%.2f".format(item.margin),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalQualificationNoticeViews(items: MutableList<SeasonalQualificationNotice>): MutableList<SeasonalQualificationNoticeView> {
    val views = mutableListOf<SeasonalQualificationNoticeView>()
    for (item in items) {
        views.add(toSeasonalQualificationNoticeView(item))
    }
    return views
}

fun sumSeasonalQualificationNoticeView(view: SeasonalQualificationNoticeView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedOdometer.toDouble() + parsedSequence.toDouble() + parsedRatio
}
