package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute preference. */
data class DisputePreferenceView(
    var code: String,
    var label: String,
    var titleText: String,
    var distanceKm: String,
    var summaryText: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toDisputePreferenceView(item: DisputePreference): DisputePreferenceView {
    return DisputePreferenceView(
        code = truncate(item.code, 80),
        label = item.label,
        titleText = item.title,
        distanceKm = "%.2f".format(item.distanceKm),
        summaryText = item.summary,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputePreferenceViews(items: MutableList<DisputePreference>): MutableList<DisputePreferenceView> {
    val views = mutableListOf<DisputePreferenceView>()
    for (item in items) {
        views.add(toDisputePreferenceView(item))
    }
    return views
}

fun sumDisputePreferenceView(view: DisputePreferenceView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedDistanceKm + parsedUpdatedAt.toDouble()
}
