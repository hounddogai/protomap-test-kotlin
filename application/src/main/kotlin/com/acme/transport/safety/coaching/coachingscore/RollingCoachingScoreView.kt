package com.acme.transport.safety.coaching.coachingscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling coaching score. */
data class RollingCoachingScoreView(
    var codeText: String,
    var nameText: String,
    var stopCountText: String,
    var ratio: String,
    var graceDaysText: String,
    var etaMinutes: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toRollingCoachingScoreView(item: RollingCoachingScore): RollingCoachingScoreView {
    return RollingCoachingScoreView(
        codeText = item.code,
        nameText = item.name,
        stopCountText = item.stopCount.toString(),
        ratio = "%.2f".format(item.ratio),
        graceDaysText = item.graceDays.toString(),
        etaMinutes = item.etaMinutes.toString(),
        checksumText = truncate(item.checksum, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCoachingScoreViews(items: MutableList<RollingCoachingScore>): MutableList<RollingCoachingScoreView> {
    val views = mutableListOf<RollingCoachingScoreView>()
    for (item in items) {
        views.add(toRollingCoachingScoreView(item))
    }
    return views
}

fun sumRollingCoachingScoreView(view: RollingCoachingScoreView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedStopCount.toDouble() + parsedRatio + parsedGraceDays.toDouble()
}
