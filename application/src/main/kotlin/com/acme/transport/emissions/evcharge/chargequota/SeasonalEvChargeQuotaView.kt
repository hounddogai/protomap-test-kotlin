package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal ev charge quota. */
data class SeasonalEvChargeQuotaView(
    var code: String,
    var labelText: String,
    var attempts: String,
    var approved: String,
    var name: String,
    var shiftCode: String,
    var summaryText: String,
    var active: String,
    var visible: String,
    var etaMinutes: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toSeasonalEvChargeQuotaView(item: SeasonalEvChargeQuota): SeasonalEvChargeQuotaView {
    return SeasonalEvChargeQuotaView(
        code = item.code,
        labelText = item.label,
        attempts = item.attempts.toString(),
        approved = "${item.approved}",
        name = item.name,
        shiftCode = truncate(item.shiftCode, 40),
        summaryText = item.summary,
        active = "${item.active}",
        visible = "${item.visible}",
        etaMinutes = item.etaMinutes.toString(),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalEvChargeQuotaViews(items: MutableList<SeasonalEvChargeQuota>): MutableList<SeasonalEvChargeQuotaView> {
    val views = mutableListOf<SeasonalEvChargeQuotaView>()
    for (item in items) {
        views.add(toSeasonalEvChargeQuotaView(item))
    }
    return views
}

fun sumSeasonalEvChargeQuotaView(view: SeasonalEvChargeQuotaView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedAttempts.toDouble() + parsedEtaMinutes.toDouble() + parsedStartsAt.toDouble()
}
