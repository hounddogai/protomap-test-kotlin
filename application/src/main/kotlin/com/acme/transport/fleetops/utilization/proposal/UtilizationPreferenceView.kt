package com.acme.transport.fleetops.utilization.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization preference. */
data class UtilizationPreferenceView(
    var code: String,
    var title: String,
    var graceDays: String,
    var localeText: String,
    var checksum: String,
    var ratioText: String,
    var currencyText: String,
    var createdAtText: String,
    var reviewerText: String,
    var pinnedText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toUtilizationPreferenceView(item: UtilizationPreference): UtilizationPreferenceView {
    return UtilizationPreferenceView(
        code = truncate(item.code, 40),
        title = item.title,
        graceDays = item.graceDays.toString(),
        localeText = truncate(item.locale, 80),
        checksum = item.checksum,
        ratioText = "%.2f".format(item.ratio),
        currencyText = item.currency,
        createdAtText = item.createdAt.toString(),
        reviewerText = truncate(item.reviewer, 40),
        pinnedText = "${item.pinned}",
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationPreferenceViews(items: MutableList<UtilizationPreference>): MutableList<UtilizationPreferenceView> {
    val views = mutableListOf<UtilizationPreferenceView>()
    for (item in items) {
        views.add(toUtilizationPreferenceView(item))
    }
    return views
}

fun sumUtilizationPreferenceView(view: UtilizationPreferenceView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedGraceDays.toDouble() + parsedRatio + parsedCreatedAt.toDouble()
}
