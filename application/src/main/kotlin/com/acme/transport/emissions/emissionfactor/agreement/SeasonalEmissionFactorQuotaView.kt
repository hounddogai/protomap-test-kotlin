package com.acme.transport.emissions.emissionfactor.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal emission factor quota. */
data class SeasonalEmissionFactorQuotaView(
    var codeText: String,
    var nameText: String,
    var timezone: String,
    var countryText: String,
    var source: String,
    var contactEmail: String,
    var locale: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toSeasonalEmissionFactorQuotaView(item: SeasonalEmissionFactorQuota): SeasonalEmissionFactorQuotaView {
    return SeasonalEmissionFactorQuotaView(
        codeText = item.code,
        nameText = item.name,
        timezone = item.timezone,
        countryText = item.country,
        source = item.source,
        contactEmail = truncate(item.contactEmail, 64),
        locale = item.locale,
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalEmissionFactorQuotaViews(items: MutableList<SeasonalEmissionFactorQuota>): MutableList<SeasonalEmissionFactorQuotaView> {
    val views = mutableListOf<SeasonalEmissionFactorQuotaView>()
    for (item in items) {
        views.add(toSeasonalEmissionFactorQuotaView(item))
    }
    return views
}

fun sumSeasonalEmissionFactorQuotaView(view: SeasonalEmissionFactorQuotaView): Double {
    val parsedStopCount = view.stopCount.toInt()
    return parsedStopCount.toDouble()
}
