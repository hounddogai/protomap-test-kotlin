package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual coaching ledger. */
data class ManualCoachingLedgerView(
    var code: String,
    var titleText: String,
    var version: String,
    var budget: String,
    var durationMinutesText: String,
    var dock: String,
    var description: String,
    var timezone: String,
    var segment: String,
    var active: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toManualCoachingLedgerView(item: ManualCoachingLedger): ManualCoachingLedgerView {
    return ManualCoachingLedgerView(
        code = truncate(item.code, 80),
        titleText = truncate(item.title, 120),
        version = item.version.toString(),
        budget = "%.2f".format(item.budget),
        durationMinutesText = item.durationMinutes.toString(),
        dock = item.dock,
        description = item.description,
        timezone = item.timezone,
        segment = item.segment,
        active = "${item.active}",
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCoachingLedgerViews(items: MutableList<ManualCoachingLedger>): MutableList<ManualCoachingLedgerView> {
    val views = mutableListOf<ManualCoachingLedgerView>()
    for (item in items) {
        views.add(toManualCoachingLedgerView(item))
    }
    return views
}

fun sumManualCoachingLedgerView(view: ManualCoachingLedgerView): Double {
    val parsedVersion = view.version.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedVersion.toDouble() + parsedBudget + parsedDurationMinutes.toDouble()
}
