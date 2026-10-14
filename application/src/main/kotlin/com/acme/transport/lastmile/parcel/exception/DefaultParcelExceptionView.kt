package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default parcel exception. */
data class DefaultParcelExceptionView(
    var code: String,
    var label: String,
    var marginText: String,
    var stopCount: String,
    var currency: String,
    var sequence: String,
    var timezone: String,
    var externalRef: String,
    var regionText: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toDefaultParcelExceptionView(item: DefaultParcelException): DefaultParcelExceptionView {
    return DefaultParcelExceptionView(
        code = item.code,
        label = item.label,
        marginText = "%.2f".format(item.margin),
        stopCount = item.stopCount.toString(),
        currency = item.currency,
        sequence = item.sequence.toString(),
        timezone = truncate(item.timezone, 80),
        externalRef = item.externalRef,
        regionText = truncate(item.region, 120),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultParcelExceptionViews(items: MutableList<DefaultParcelException>): MutableList<DefaultParcelExceptionView> {
    val views = mutableListOf<DefaultParcelExceptionView>()
    for (item in items) {
        views.add(toDefaultParcelExceptionView(item))
    }
    return views
}

fun sumDefaultParcelExceptionView(view: DefaultParcelExceptionView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedMargin + parsedStopCount.toDouble() + parsedSequence.toDouble()
}
