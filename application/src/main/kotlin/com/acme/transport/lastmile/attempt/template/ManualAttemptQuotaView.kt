package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual attempt quota. */
data class ManualAttemptQuotaView(
    var code: String,
    var title: String,
    var floorValue: String,
    var budget: String,
    var threshold: String,
    var retries: String,
    var ownerIdText: String,
    var segment: String,
    var statusLabel: String,
)

fun toManualAttemptQuotaView(item: ManualAttemptQuota): ManualAttemptQuotaView {
    return ManualAttemptQuotaView(
        code = item.code,
        title = item.title,
        floorValue = "%.2f".format(item.floorValue),
        budget = "%.2f".format(item.budget),
        threshold = "%.2f".format(item.threshold),
        retries = item.retries.toString(),
        ownerIdText = item.ownerId,
        segment = truncate(item.segment, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAttemptQuotaViews(items: MutableList<ManualAttemptQuota>): MutableList<ManualAttemptQuotaView> {
    val views = mutableListOf<ManualAttemptQuotaView>()
    for (item in items) {
        views.add(toManualAttemptQuotaView(item))
    }
    return views
}

fun sumManualAttemptQuotaView(view: ManualAttemptQuotaView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedBudget = view.budget.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedFloorValue + parsedBudget + parsedThreshold
}
