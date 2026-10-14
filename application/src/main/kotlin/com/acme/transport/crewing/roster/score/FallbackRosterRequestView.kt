package com.acme.transport.crewing.roster.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback roster request. */
data class FallbackRosterRequestView(
    var code: String,
    var name: String,
    var etaMinutes: String,
    var retriesText: String,
    var depot: String,
    var revision: String,
    var shiftCodeText: String,
    var trailerId: String,
    var locale: String,
    var countryText: String,
    var statusLabel: String,
)

fun toFallbackRosterRequestView(item: FallbackRosterRequest): FallbackRosterRequestView {
    return FallbackRosterRequestView(
        code = item.code,
        name = truncate(item.name, 40),
        etaMinutes = item.etaMinutes.toString(),
        retriesText = item.retries.toString(),
        depot = item.depot,
        revision = item.revision.toString(),
        shiftCodeText = truncate(item.shiftCode, 80),
        trailerId = item.trailerId,
        locale = item.locale,
        countryText = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRosterRequestViews(items: MutableList<FallbackRosterRequest>): MutableList<FallbackRosterRequestView> {
    val views = mutableListOf<FallbackRosterRequestView>()
    for (item in items) {
        views.add(toFallbackRosterRequestView(item))
    }
    return views
}

fun sumFallbackRosterRequestView(view: FallbackRosterRequestView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedEtaMinutes.toDouble() + parsedRetries.toDouble() + parsedRevision.toDouble()
}
