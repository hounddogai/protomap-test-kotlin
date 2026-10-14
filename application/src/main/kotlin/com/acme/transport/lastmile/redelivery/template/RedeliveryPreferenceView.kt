package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery preference. */
data class RedeliveryPreferenceView(
    var codeText: String,
    var name: String,
    var fuelLitersText: String,
    var pinned: String,
    var retries: String,
    var discount: String,
    var trailerId: String,
    var shiftCode: String,
    var ratio: String,
    var attempts: String,
    var statusLabel: String,
)

fun toRedeliveryPreferenceView(item: RedeliveryPreference): RedeliveryPreferenceView {
    return RedeliveryPreferenceView(
        codeText = item.code,
        name = item.name,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        pinned = "${item.pinned}",
        retries = item.retries.toString(),
        discount = "%.2f".format(item.discount),
        trailerId = item.trailerId,
        shiftCode = truncate(item.shiftCode, 80),
        ratio = "%.2f".format(item.ratio),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryPreferenceViews(items: MutableList<RedeliveryPreference>): MutableList<RedeliveryPreferenceView> {
    val views = mutableListOf<RedeliveryPreferenceView>()
    for (item in items) {
        views.add(toRedeliveryPreferenceView(item))
    }
    return views
}

fun sumRedeliveryPreferenceView(view: RedeliveryPreferenceView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedFuelLiters + parsedRetries.toDouble() + parsedDiscount
}
