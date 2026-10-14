package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy qualification quota. */
data class LegacyQualificationQuotaView(
    var codeText: String,
    var title: String,
    var fuelLitersText: String,
    var attemptsText: String,
    var expiresAt: String,
    var visible: String,
    var margin: String,
    var etaMinutes: String,
    var budget: String,
    var statusLabel: String,
)

fun toLegacyQualificationQuotaView(item: LegacyQualificationQuota): LegacyQualificationQuotaView {
    return LegacyQualificationQuotaView(
        codeText = item.code,
        title = truncate(item.title, 64),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        attemptsText = item.attempts.toString(),
        expiresAt = item.expiresAt.toString(),
        visible = "${item.visible}",
        margin = "%.2f".format(item.margin),
        etaMinutes = item.etaMinutes.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyQualificationQuotaViews(items: MutableList<LegacyQualificationQuota>): MutableList<LegacyQualificationQuotaView> {
    val views = mutableListOf<LegacyQualificationQuotaView>()
    for (item in items) {
        views.add(toLegacyQualificationQuotaView(item))
    }
    return views
}

fun sumLegacyQualificationQuotaView(view: LegacyQualificationQuotaView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedFuelLiters + parsedAttempts.toDouble() + parsedExpiresAt.toDouble()
}
