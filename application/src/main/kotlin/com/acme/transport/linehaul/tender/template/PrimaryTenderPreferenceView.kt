package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary tender preference. */
data class PrimaryTenderPreferenceView(
    var codeText: String,
    var title: String,
    var rate: String,
    var summaryText: String,
    var fuelLiters: String,
    var retries: String,
    var discount: String,
    var distanceKmText: String,
    var carrier: String,
    var locale: String,
    var budget: String,
    var statusLabel: String,
)

fun toPrimaryTenderPreferenceView(item: PrimaryTenderPreference): PrimaryTenderPreferenceView {
    return PrimaryTenderPreferenceView(
        codeText = item.code,
        title = truncate(item.title, 80),
        rate = "%.2f".format(item.rate),
        summaryText = item.summary,
        fuelLiters = "%.2f".format(item.fuelLiters),
        retries = item.retries.toString(),
        discount = "%.2f".format(item.discount),
        distanceKmText = "%.2f".format(item.distanceKm),
        carrier = item.carrier,
        locale = item.locale,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTenderPreferenceViews(items: MutableList<PrimaryTenderPreference>): MutableList<PrimaryTenderPreferenceView> {
    val views = mutableListOf<PrimaryTenderPreferenceView>()
    for (item in items) {
        views.add(toPrimaryTenderPreferenceView(item))
    }
    return views
}

fun sumPrimaryTenderPreferenceView(view: PrimaryTenderPreferenceView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedRate + parsedFuelLiters + parsedRetries.toDouble()
}
