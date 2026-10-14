package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal roster template. */
data class SeasonalRosterTemplateView(
    var code: String,
    var labelText: String,
    var countryText: String,
    var phoneText: String,
    var externalRef: String,
    var channel: String,
    var timezone: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toSeasonalRosterTemplateView(item: SeasonalRosterTemplate): SeasonalRosterTemplateView {
    return SeasonalRosterTemplateView(
        code = item.code,
        labelText = truncate(item.label, 40),
        countryText = truncate(item.country, 64),
        phoneText = item.phone,
        externalRef = item.externalRef,
        channel = item.channel,
        timezone = item.timezone,
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRosterTemplateViews(items: MutableList<SeasonalRosterTemplate>): MutableList<SeasonalRosterTemplateView> {
    val views = mutableListOf<SeasonalRosterTemplateView>()
    for (item in items) {
        views.add(toSeasonalRosterTemplateView(item))
    }
    return views
}

fun sumSeasonalRosterTemplateView(view: SeasonalRosterTemplateView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedEtaMinutes.toDouble()
}
