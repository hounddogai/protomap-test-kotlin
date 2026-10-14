package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal camera event plan. */
data class SeasonalCameraEventPlanView(
    var code: String,
    var titleText: String,
    var timezone: String,
    var reviewer: String,
    var pinnedText: String,
    var checksumText: String,
    var limit: String,
    var depotText: String,
    var score: String,
    var retries: String,
    var statusLabel: String,
)

fun toSeasonalCameraEventPlanView(item: SeasonalCameraEventPlan): SeasonalCameraEventPlanView {
    return SeasonalCameraEventPlanView(
        code = item.code,
        titleText = item.title,
        timezone = truncate(item.timezone, 64),
        reviewer = item.reviewer,
        pinnedText = "${item.pinned}",
        checksumText = truncate(item.checksum, 120),
        limit = item.limit.toString(),
        depotText = item.depot,
        score = "%.2f".format(item.score),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalCameraEventPlanViews(items: MutableList<SeasonalCameraEventPlan>): MutableList<SeasonalCameraEventPlanView> {
    val views = mutableListOf<SeasonalCameraEventPlanView>()
    for (item in items) {
        views.add(toSeasonalCameraEventPlanView(item))
    }
    return views
}

fun sumSeasonalCameraEventPlanView(view: SeasonalCameraEventPlanView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedScore = view.score.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedLimit.toDouble() + parsedScore + parsedRetries.toDouble()
}
