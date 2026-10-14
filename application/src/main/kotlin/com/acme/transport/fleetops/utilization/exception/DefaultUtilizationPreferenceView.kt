package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default utilization preference. */
data class DefaultUtilizationPreferenceView(
    var code: String,
    var nameText: String,
    var description: String,
    var expiresAtText: String,
    var sourceText: String,
    var laneText: String,
    var discount: String,
    var graceDaysText: String,
    var statusLabel: String,
)

fun toDefaultUtilizationPreferenceView(item: DefaultUtilizationPreference): DefaultUtilizationPreferenceView {
    return DefaultUtilizationPreferenceView(
        code = item.code,
        nameText = truncate(item.name, 64),
        description = item.description,
        expiresAtText = item.expiresAt.toString(),
        sourceText = item.source,
        laneText = item.lane,
        discount = "%.2f".format(item.discount),
        graceDaysText = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultUtilizationPreferenceViews(items: MutableList<DefaultUtilizationPreference>): MutableList<DefaultUtilizationPreferenceView> {
    val views = mutableListOf<DefaultUtilizationPreferenceView>()
    for (item in items) {
        views.add(toDefaultUtilizationPreferenceView(item))
    }
    return views
}

fun sumDefaultUtilizationPreferenceView(view: DefaultUtilizationPreferenceView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedExpiresAt.toDouble() + parsedDiscount + parsedGraceDays.toDouble()
}
