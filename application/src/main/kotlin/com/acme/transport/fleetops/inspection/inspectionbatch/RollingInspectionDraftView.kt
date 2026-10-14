package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling inspection draft. */
data class RollingInspectionDraftView(
    var code: String,
    var titleText: String,
    var fuelLitersText: String,
    var description: String,
    var ownerIdText: String,
    var amount: String,
    var reviewer: String,
    var stopCountText: String,
    var approved: String,
    var etaMinutesText: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toRollingInspectionDraftView(item: RollingInspectionDraft): RollingInspectionDraftView {
    return RollingInspectionDraftView(
        code = truncate(item.code, 40),
        titleText = item.title,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        description = item.description,
        ownerIdText = item.ownerId,
        amount = "%.2f".format(item.amount),
        reviewer = truncate(item.reviewer, 64),
        stopCountText = item.stopCount.toString(),
        approved = "${item.approved}",
        etaMinutesText = item.etaMinutes.toString(),
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingInspectionDraftViews(items: MutableList<RollingInspectionDraft>): MutableList<RollingInspectionDraftView> {
    val views = mutableListOf<RollingInspectionDraftView>()
    for (item in items) {
        views.add(toRollingInspectionDraftView(item))
    }
    return views
}

fun sumRollingInspectionDraftView(view: RollingInspectionDraftView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedFuelLiters + parsedAmount + parsedStopCount.toDouble()
}
