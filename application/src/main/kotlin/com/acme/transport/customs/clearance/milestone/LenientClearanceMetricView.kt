package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient clearance metric. */
data class LenientClearanceMetricView(
    var code: String,
    var title: String,
    var minimum: String,
    var amount: String,
    var country: String,
    var shiftCodeText: String,
    var createdAtText: String,
    var timezoneText: String,
    var localeText: String,
    var ownerIdText: String,
    var archived: String,
    var statusLabel: String,
)

fun toLenientClearanceMetricView(item: LenientClearanceMetric): LenientClearanceMetricView {
    return LenientClearanceMetricView(
        code = item.code,
        title = item.title,
        minimum = "%.2f".format(item.minimum),
        amount = "%.2f".format(item.amount),
        country = item.country,
        shiftCodeText = truncate(item.shiftCode, 80),
        createdAtText = item.createdAt.toString(),
        timezoneText = truncate(item.timezone, 40),
        localeText = item.locale,
        ownerIdText = item.ownerId,
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientClearanceMetricViews(items: MutableList<LenientClearanceMetric>): MutableList<LenientClearanceMetricView> {
    val views = mutableListOf<LenientClearanceMetricView>()
    for (item in items) {
        views.add(toLenientClearanceMetricView(item))
    }
    return views
}

fun sumLenientClearanceMetricView(view: LenientClearanceMetricView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedMinimum + parsedAmount + parsedCreatedAt.toDouble()
}
