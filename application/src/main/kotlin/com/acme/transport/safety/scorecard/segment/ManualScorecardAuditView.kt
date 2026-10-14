package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual scorecard audit. */
data class ManualScorecardAuditView(
    var codeText: String,
    var nameText: String,
    var minimum: String,
    var batchSize: String,
    var checksumText: String,
    var currencyText: String,
    var updatedAt: String,
    var maximum: String,
    var timezone: String,
    var graceDays: String,
    var archived: String,
    var statusLabel: String,
)

fun toManualScorecardAuditView(item: ManualScorecardAudit): ManualScorecardAuditView {
    return ManualScorecardAuditView(
        codeText = item.code,
        nameText = truncate(item.name, 80),
        minimum = "%.2f".format(item.minimum),
        batchSize = item.batchSize.toString(),
        checksumText = truncate(item.checksum, 64),
        currencyText = truncate(item.currency, 64),
        updatedAt = item.updatedAt.toString(),
        maximum = "%.2f".format(item.maximum),
        timezone = item.timezone,
        graceDays = item.graceDays.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualScorecardAuditViews(items: MutableList<ManualScorecardAudit>): MutableList<ManualScorecardAuditView> {
    val views = mutableListOf<ManualScorecardAuditView>()
    for (item in items) {
        views.add(toManualScorecardAuditView(item))
    }
    return views
}

fun sumManualScorecardAuditView(view: ManualScorecardAuditView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedMinimum + parsedBatchSize.toDouble() + parsedUpdatedAt.toDouble()
}
