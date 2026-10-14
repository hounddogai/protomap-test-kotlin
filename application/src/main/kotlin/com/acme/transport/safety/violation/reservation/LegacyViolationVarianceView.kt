package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy violation variance. */
data class LegacyViolationVarianceView(
    var codeText: String,
    var title: String,
    var updatedAtText: String,
    var pallets: String,
    var label: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toLegacyViolationVarianceView(item: LegacyViolationVariance): LegacyViolationVarianceView {
    return LegacyViolationVarianceView(
        codeText = truncate(item.code, 64),
        title = item.title,
        updatedAtText = item.updatedAt.toString(),
        pallets = item.pallets.toString(),
        label = truncate(item.label, 120),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyViolationVarianceViews(items: MutableList<LegacyViolationVariance>): MutableList<LegacyViolationVarianceView> {
    val views = mutableListOf<LegacyViolationVarianceView>()
    for (item in items) {
        views.add(toLegacyViolationVarianceView(item))
    }
    return views
}

fun sumLegacyViolationVarianceView(view: LegacyViolationVarianceView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedUpdatedAt.toDouble() + parsedPallets.toDouble() + parsedStartsAt.toDouble()
}
