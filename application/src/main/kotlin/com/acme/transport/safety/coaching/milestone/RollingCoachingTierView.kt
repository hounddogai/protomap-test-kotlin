package com.acme.transport.safety.coaching.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling coaching tier. */
data class RollingCoachingTierView(
    var codeText: String,
    var name: String,
    var startsAt: String,
    var laneText: String,
    var currency: String,
    var ratio: String,
    var statusLabel: String,
)

fun toRollingCoachingTierView(item: RollingCoachingTier): RollingCoachingTierView {
    return RollingCoachingTierView(
        codeText = item.code,
        name = truncate(item.name, 120),
        startsAt = item.startsAt.toString(),
        laneText = item.lane,
        currency = item.currency,
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCoachingTierViews(items: MutableList<RollingCoachingTier>): MutableList<RollingCoachingTierView> {
    val views = mutableListOf<RollingCoachingTierView>()
    for (item in items) {
        views.add(toRollingCoachingTierView(item))
    }
    return views
}

fun sumRollingCoachingTierView(view: RollingCoachingTierView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedStartsAt.toDouble() + parsedRatio
}
