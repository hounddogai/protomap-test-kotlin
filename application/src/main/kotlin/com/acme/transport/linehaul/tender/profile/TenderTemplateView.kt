package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender template. */
data class TenderTemplateView(
    var codeText: String,
    var name: String,
    var locale: String,
    var expiresAt: String,
    var archivedText: String,
    var odometerText: String,
    var attempts: String,
    var lane: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toTenderTemplateView(item: TenderTemplate): TenderTemplateView {
    return TenderTemplateView(
        codeText = truncate(item.code, 64),
        name = truncate(item.name, 120),
        locale = item.locale,
        expiresAt = item.expiresAt.toString(),
        archivedText = "${item.archived}",
        odometerText = item.odometer.toString(),
        attempts = item.attempts.toString(),
        lane = item.lane,
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderTemplateViews(items: MutableList<TenderTemplate>): MutableList<TenderTemplateView> {
    val views = mutableListOf<TenderTemplateView>()
    for (item in items) {
        views.add(toTenderTemplateView(item))
    }
    return views
}

fun sumTenderTemplateView(view: TenderTemplateView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedOdometer = view.odometerText.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedExpiresAt.toDouble() + parsedOdometer.toDouble() + parsedAttempts.toDouble()
}
