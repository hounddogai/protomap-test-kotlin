package com.acme.transport.lastmile.redelivery.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal redelivery notice. */
data class SeasonalRedeliveryNoticeView(
    var code: String,
    var title: String,
    var timezone: String,
    var approved: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toSeasonalRedeliveryNoticeView(item: SeasonalRedeliveryNotice): SeasonalRedeliveryNoticeView {
    return SeasonalRedeliveryNoticeView(
        code = item.code,
        title = truncate(item.title, 64),
        timezone = truncate(item.timezone, 120),
        approved = "${item.approved}",
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRedeliveryNoticeViews(items: MutableList<SeasonalRedeliveryNotice>): MutableList<SeasonalRedeliveryNoticeView> {
    val views = mutableListOf<SeasonalRedeliveryNoticeView>()
    for (item in items) {
        views.add(toSeasonalRedeliveryNoticeView(item))
    }
    return views
}

fun sumSeasonalRedeliveryNoticeView(view: SeasonalRedeliveryNoticeView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedFuelLiters
}
