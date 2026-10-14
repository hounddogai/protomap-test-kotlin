package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict violation forecast. */
data class StrictViolationForecastView(
    var code: String,
    var titleText: String,
    var revisionText: String,
    var retries: String,
    var contactEmail: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toStrictViolationForecastView(item: StrictViolationForecast): StrictViolationForecastView {
    return StrictViolationForecastView(
        code = item.code,
        titleText = truncate(item.title, 64),
        revisionText = item.revision.toString(),
        retries = item.retries.toString(),
        contactEmail = item.contactEmail,
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictViolationForecastViews(items: MutableList<StrictViolationForecast>): MutableList<StrictViolationForecastView> {
    val views = mutableListOf<StrictViolationForecastView>()
    for (item in items) {
        views.add(toStrictViolationForecastView(item))
    }
    return views
}

fun sumStrictViolationForecastView(view: StrictViolationForecastView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedRevision.toDouble() + parsedRetries.toDouble() + parsedEtaMinutes.toDouble()
}
