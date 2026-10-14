package com.acme.transport.linehaul.trailer.trailerrecord

/** How screens and exports show a legacy trailer milestone. */
data class LegacyTrailerMilestoneView(
    var code: String,
    var titleText: String,
    var rate: String,
    var score: String,
    var ownerId: String,
    var timezone: String,
    var statusLabel: String,
)

fun toLegacyTrailerMilestoneView(item: LegacyTrailerMilestone): LegacyTrailerMilestoneView {
    return LegacyTrailerMilestoneView(
        code = item.code,
        titleText = item.title,
        rate = "%.2f".format(item.rate),
        score = "%.2f".format(item.score),
        ownerId = item.ownerId,
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTrailerMilestoneViews(items: MutableList<LegacyTrailerMilestone>): MutableList<LegacyTrailerMilestoneView> {
    val views = mutableListOf<LegacyTrailerMilestoneView>()
    for (item in items) {
        views.add(toLegacyTrailerMilestoneView(item))
    }
    return views
}

fun sumLegacyTrailerMilestoneView(view: LegacyTrailerMilestoneView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedRate + parsedScore
}
