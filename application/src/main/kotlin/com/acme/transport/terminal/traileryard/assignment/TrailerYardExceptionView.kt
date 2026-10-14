package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard exception. */
data class TrailerYardExceptionView(
    var codeText: String,
    var label: String,
    var titleText: String,
    var description: String,
    var rate: String,
    var ownerId: String,
    var loadKg: String,
    var carrier: String,
    var statusLabel: String,
)

fun toTrailerYardExceptionView(item: TrailerYardException): TrailerYardExceptionView {
    return TrailerYardExceptionView(
        codeText = item.code,
        label = truncate(item.label, 40),
        titleText = item.title,
        description = truncate(item.description, 80),
        rate = "%.2f".format(item.rate),
        ownerId = item.ownerId,
        loadKg = "%.2f".format(item.loadKg),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardExceptionViews(items: MutableList<TrailerYardException>): MutableList<TrailerYardExceptionView> {
    val views = mutableListOf<TrailerYardExceptionView>()
    for (item in items) {
        views.add(toTrailerYardExceptionView(item))
    }
    return views
}

fun sumTrailerYardExceptionView(view: TrailerYardExceptionView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedRate + parsedLoadKg
}
