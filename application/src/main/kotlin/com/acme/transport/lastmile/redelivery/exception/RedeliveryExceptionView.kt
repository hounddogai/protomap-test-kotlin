package com.acme.transport.lastmile.redelivery.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery exception. */
data class RedeliveryExceptionView(
    var codeText: String,
    var titleText: String,
    var updatedAt: String,
    var approved: String,
    var odometer: String,
    var summary: String,
    var currencyText: String,
    var externalRef: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toRedeliveryExceptionView(item: RedeliveryException): RedeliveryExceptionView {
    return RedeliveryExceptionView(
        codeText = truncate(item.code, 80),
        titleText = item.title,
        updatedAt = item.updatedAt.toString(),
        approved = "${item.approved}",
        odometer = item.odometer.toString(),
        summary = item.summary,
        currencyText = item.currency,
        externalRef = item.externalRef,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryExceptionViews(items: MutableList<RedeliveryException>): MutableList<RedeliveryExceptionView> {
    val views = mutableListOf<RedeliveryExceptionView>()
    for (item in items) {
        views.add(toRedeliveryExceptionView(item))
    }
    return views
}

fun sumRedeliveryExceptionView(view: RedeliveryExceptionView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedUpdatedAt.toDouble() + parsedOdometer.toDouble() + parsedDistanceKm
}
