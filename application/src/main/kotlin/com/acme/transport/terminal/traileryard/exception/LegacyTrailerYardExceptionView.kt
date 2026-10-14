package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy trailer yard exception. */
data class LegacyTrailerYardExceptionView(
    var codeText: String,
    var title: String,
    var externalRefText: String,
    var routeCode: String,
    var margin: String,
    var segment: String,
    var checksum: String,
    var depot: String,
    var retries: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toLegacyTrailerYardExceptionView(item: LegacyTrailerYardException): LegacyTrailerYardExceptionView {
    return LegacyTrailerYardExceptionView(
        codeText = item.code,
        title = truncate(item.title, 64),
        externalRefText = truncate(item.externalRef, 64),
        routeCode = item.routeCode,
        margin = "%.2f".format(item.margin),
        segment = truncate(item.segment, 120),
        checksum = item.checksum,
        depot = item.depot,
        retries = item.retries.toString(),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTrailerYardExceptionViews(items: MutableList<LegacyTrailerYardException>): MutableList<LegacyTrailerYardExceptionView> {
    val views = mutableListOf<LegacyTrailerYardExceptionView>()
    for (item in items) {
        views.add(toLegacyTrailerYardExceptionView(item))
    }
    return views
}

fun sumLegacyTrailerYardExceptionView(view: LegacyTrailerYardExceptionView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedMargin + parsedRetries.toDouble() + parsedDurationMinutes.toDouble()
}
