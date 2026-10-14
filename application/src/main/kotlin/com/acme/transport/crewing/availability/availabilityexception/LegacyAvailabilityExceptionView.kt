package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy availability exception. */
data class LegacyAvailabilityExceptionView(
    var codeText: String,
    var name: String,
    var revisionText: String,
    var vehicleIdText: String,
    var margin: String,
    var amount: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toLegacyAvailabilityExceptionView(item: LegacyAvailabilityException): LegacyAvailabilityExceptionView {
    return LegacyAvailabilityExceptionView(
        codeText = item.code,
        name = truncate(item.name, 120),
        revisionText = item.revision.toString(),
        vehicleIdText = item.vehicleId,
        margin = "%.2f".format(item.margin),
        amount = "%.2f".format(item.amount),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyAvailabilityExceptionViews(items: MutableList<LegacyAvailabilityException>): MutableList<LegacyAvailabilityExceptionView> {
    val views = mutableListOf<LegacyAvailabilityExceptionView>()
    for (item in items) {
        views.add(toLegacyAvailabilityExceptionView(item))
    }
    return views
}

fun sumLegacyAvailabilityExceptionView(view: LegacyAvailabilityExceptionView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedRevision.toDouble() + parsedMargin + parsedAmount
}
