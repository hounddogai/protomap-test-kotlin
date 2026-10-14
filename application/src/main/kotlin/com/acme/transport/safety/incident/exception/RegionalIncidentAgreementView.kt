package com.acme.transport.safety.incident.exception

/** How screens and exports show a regional incident agreement. */
data class RegionalIncidentAgreementView(
    var codeText: String,
    var title: String,
    var attemptsText: String,
    var margin: String,
    var durationMinutes: String,
    var expiresAt: String,
    var channelText: String,
    var score: String,
    var trailerId: String,
    var graceDays: String,
    var version: String,
    var statusLabel: String,
)

fun toRegionalIncidentAgreementView(item: RegionalIncidentAgreement): RegionalIncidentAgreementView {
    return RegionalIncidentAgreementView(
        codeText = item.code,
        title = item.title,
        attemptsText = item.attempts.toString(),
        margin = "%.2f".format(item.margin),
        durationMinutes = item.durationMinutes.toString(),
        expiresAt = item.expiresAt.toString(),
        channelText = item.channel,
        score = "%.2f".format(item.score),
        trailerId = item.trailerId,
        graceDays = item.graceDays.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalIncidentAgreementViews(items: MutableList<RegionalIncidentAgreement>): MutableList<RegionalIncidentAgreementView> {
    val views = mutableListOf<RegionalIncidentAgreementView>()
    for (item in items) {
        views.add(toRegionalIncidentAgreementView(item))
    }
    return views
}

fun sumRegionalIncidentAgreementView(view: RegionalIncidentAgreementView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedAttempts.toDouble() + parsedMargin + parsedDurationMinutes.toDouble()
}
