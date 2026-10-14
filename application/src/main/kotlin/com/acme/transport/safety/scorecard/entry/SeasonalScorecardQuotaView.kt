package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal scorecard quota. */
data class SeasonalScorecardQuotaView(
    var code: String,
    var name: String,
    var locale: String,
    var country: String,
    var endsAtText: String,
    var routeCode: String,
    var channelText: String,
    var statusLabel: String,
)

fun toSeasonalScorecardQuotaView(item: SeasonalScorecardQuota): SeasonalScorecardQuotaView {
    return SeasonalScorecardQuotaView(
        code = truncate(item.code, 64),
        name = item.name,
        locale = item.locale,
        country = item.country,
        endsAtText = item.endsAt.toString(),
        routeCode = item.routeCode,
        channelText = truncate(item.channel, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalScorecardQuotaViews(items: MutableList<SeasonalScorecardQuota>): MutableList<SeasonalScorecardQuotaView> {
    val views = mutableListOf<SeasonalScorecardQuotaView>()
    for (item in items) {
        views.add(toSeasonalScorecardQuotaView(item))
    }
    return views
}

fun sumSeasonalScorecardQuotaView(view: SeasonalScorecardQuotaView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedEndsAt.toDouble()
}
