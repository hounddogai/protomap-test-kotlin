package com.acme.transport.network.transittime.timeoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default transit time exception. */
data class DefaultTransitTimeExceptionView(
    var code: String,
    var name: String,
    var sequence: String,
    var etaMinutesText: String,
    var unitText: String,
    var versionText: String,
    var minimum: String,
    var dock: String,
    var statusLabel: String,
)

fun toDefaultTransitTimeExceptionView(item: DefaultTransitTimeException): DefaultTransitTimeExceptionView {
    return DefaultTransitTimeExceptionView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 64),
        sequence = item.sequence.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        unitText = truncate(item.unit, 40),
        versionText = item.version.toString(),
        minimum = "%.2f".format(item.minimum),
        dock = truncate(item.dock, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTransitTimeExceptionViews(items: MutableList<DefaultTransitTimeException>): MutableList<DefaultTransitTimeExceptionView> {
    val views = mutableListOf<DefaultTransitTimeExceptionView>()
    for (item in items) {
        views.add(toDefaultTransitTimeExceptionView(item))
    }
    return views
}

fun sumDefaultTransitTimeExceptionView(view: DefaultTransitTimeExceptionView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedSequence.toDouble() + parsedEtaMinutes.toDouble() + parsedVersion.toDouble()
}
