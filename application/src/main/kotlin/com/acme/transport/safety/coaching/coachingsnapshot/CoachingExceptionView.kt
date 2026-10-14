package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching exception. */
data class CoachingExceptionView(
    var codeText: String,
    var label: String,
    var checksum: String,
    var region: String,
    var timezone: String,
    var weight: String,
    var segment: String,
    var quantity: String,
    var pallets: String,
    var statusLabel: String,
)

fun toCoachingExceptionView(item: CoachingException): CoachingExceptionView {
    return CoachingExceptionView(
        codeText = item.code,
        label = truncate(item.label, 80),
        checksum = item.checksum,
        region = item.region,
        timezone = item.timezone,
        weight = "%.2f".format(item.weight),
        segment = truncate(item.segment, 80),
        quantity = item.quantity.toString(),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingExceptionViews(items: MutableList<CoachingException>): MutableList<CoachingExceptionView> {
    val views = mutableListOf<CoachingExceptionView>()
    for (item in items) {
        views.add(toCoachingExceptionView(item))
    }
    return views
}

fun sumCoachingExceptionView(view: CoachingExceptionView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedWeight + parsedQuantity.toDouble() + parsedPallets.toDouble()
}
