package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered scorecard notice. */
data class TieredScorecardNoticeView(
    var code: String,
    var title: String,
    var trailerId: String,
    var ceiling: String,
    var carrier: String,
    var endsAt: String,
    var driverId: String,
    var startsAt: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toTieredScorecardNoticeView(item: TieredScorecardNotice): TieredScorecardNoticeView {
    return TieredScorecardNoticeView(
        code = item.code,
        title = truncate(item.title, 120),
        trailerId = item.trailerId,
        ceiling = "%.2f".format(item.ceiling),
        carrier = item.carrier,
        endsAt = item.endsAt.toString(),
        driverId = item.driverId,
        startsAt = item.startsAt.toString(),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredScorecardNoticeViews(items: MutableList<TieredScorecardNotice>): MutableList<TieredScorecardNoticeView> {
    val views = mutableListOf<TieredScorecardNoticeView>()
    for (item in items) {
        views.add(toTieredScorecardNoticeView(item))
    }
    return views
}

fun sumTieredScorecardNoticeView(view: TieredScorecardNoticeView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedCeiling + parsedEndsAt.toDouble() + parsedStartsAt.toDouble()
}
