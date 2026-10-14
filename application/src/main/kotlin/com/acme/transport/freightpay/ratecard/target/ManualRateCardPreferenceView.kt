package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual rate card preference. */
data class ManualRateCardPreferenceView(
    var code: String,
    var label: String,
    var checksum: String,
    var contactEmailText: String,
    var marginText: String,
    var weight: String,
    var depot: String,
    var createdAt: String,
    var externalRefText: String,
    var trailerId: String,
    var localeText: String,
    var statusLabel: String,
)

fun toManualRateCardPreferenceView(item: ManualRateCardPreference): ManualRateCardPreferenceView {
    return ManualRateCardPreferenceView(
        code = item.code,
        label = item.label,
        checksum = item.checksum,
        contactEmailText = item.contactEmail,
        marginText = "%.2f".format(item.margin),
        weight = "%.2f".format(item.weight),
        depot = truncate(item.depot, 64),
        createdAt = item.createdAt.toString(),
        externalRefText = item.externalRef,
        trailerId = item.trailerId,
        localeText = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualRateCardPreferenceViews(items: MutableList<ManualRateCardPreference>): MutableList<ManualRateCardPreferenceView> {
    val views = mutableListOf<ManualRateCardPreferenceView>()
    for (item in items) {
        views.add(toManualRateCardPreferenceView(item))
    }
    return views
}

fun sumManualRateCardPreferenceView(view: ManualRateCardPreferenceView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedMargin + parsedWeight + parsedCreatedAt.toDouble()
}
