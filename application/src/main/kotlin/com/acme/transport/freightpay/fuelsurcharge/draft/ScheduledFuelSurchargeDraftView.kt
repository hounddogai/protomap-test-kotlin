package com.acme.transport.freightpay.fuelsurcharge.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled fuel surcharge draft. */
data class ScheduledFuelSurchargeDraftView(
    var code: String,
    var titleText: String,
    var trailerId: String,
    var endsAtText: String,
    var score: String,
    var carrierText: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toScheduledFuelSurchargeDraftView(item: ScheduledFuelSurchargeDraft): ScheduledFuelSurchargeDraftView {
    return ScheduledFuelSurchargeDraftView(
        code = item.code,
        titleText = item.title,
        trailerId = item.trailerId,
        endsAtText = item.endsAt.toString(),
        score = "%.2f".format(item.score),
        carrierText = item.carrier,
        checksumText = truncate(item.checksum, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledFuelSurchargeDraftViews(items: MutableList<ScheduledFuelSurchargeDraft>): MutableList<ScheduledFuelSurchargeDraftView> {
    val views = mutableListOf<ScheduledFuelSurchargeDraftView>()
    for (item in items) {
        views.add(toScheduledFuelSurchargeDraftView(item))
    }
    return views
}

fun sumScheduledFuelSurchargeDraftView(view: ScheduledFuelSurchargeDraftView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedEndsAt.toDouble() + parsedScore
}
