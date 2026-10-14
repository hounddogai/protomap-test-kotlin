package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual fuel surcharge milestone. */
data class ManualFuelSurchargeMilestoneView(
    var code: String,
    var title: String,
    var country: String,
    var locale: String,
    var stopCount: String,
    var limit: String,
    var lane: String,
    var notesText: String,
    var expiresAt: String,
    var margin: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toManualFuelSurchargeMilestoneView(item: ManualFuelSurchargeMilestone): ManualFuelSurchargeMilestoneView {
    return ManualFuelSurchargeMilestoneView(
        code = item.code,
        title = truncate(item.title, 64),
        country = truncate(item.country, 40),
        locale = item.locale,
        stopCount = item.stopCount.toString(),
        limit = item.limit.toString(),
        lane = item.lane,
        notesText = truncate(item.notes, 80),
        expiresAt = item.expiresAt.toString(),
        margin = "%.2f".format(item.margin),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualFuelSurchargeMilestoneViews(items: MutableList<ManualFuelSurchargeMilestone>): MutableList<ManualFuelSurchargeMilestoneView> {
    val views = mutableListOf<ManualFuelSurchargeMilestoneView>()
    for (item in items) {
        views.add(toManualFuelSurchargeMilestoneView(item))
    }
    return views
}

fun sumManualFuelSurchargeMilestoneView(view: ManualFuelSurchargeMilestoneView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedStopCount.toDouble() + parsedLimit.toDouble() + parsedExpiresAt.toDouble()
}
