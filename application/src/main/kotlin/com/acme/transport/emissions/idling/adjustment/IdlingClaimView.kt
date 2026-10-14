package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling claim. */
data class IdlingClaimView(
    var code: String,
    var title: String,
    var phone: String,
    var ceiling: String,
    var ownerId: String,
    var stopCountText: String,
    var lane: String,
    var sourceText: String,
    var ratio: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toIdlingClaimView(item: IdlingClaim): IdlingClaimView {
    return IdlingClaimView(
        code = item.code,
        title = item.title,
        phone = item.phone,
        ceiling = "%.2f".format(item.ceiling),
        ownerId = item.ownerId,
        stopCountText = item.stopCount.toString(),
        lane = item.lane,
        sourceText = truncate(item.source, 120),
        ratio = "%.2f".format(item.ratio),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingClaimViews(items: MutableList<IdlingClaim>): MutableList<IdlingClaimView> {
    val views = mutableListOf<IdlingClaimView>()
    for (item in items) {
        views.add(toIdlingClaimView(item))
    }
    return views
}

fun sumIdlingClaimView(view: IdlingClaimView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedCeiling + parsedStopCount.toDouble() + parsedRatio
}
