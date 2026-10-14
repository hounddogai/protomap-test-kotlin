package com.acme.transport.fleetops.tire.tier

/** How screens and exports show a scheduled tire score. */
data class ScheduledTireScoreView(
    var codeText: String,
    var title: String,
    var dock: String,
    var graceDaysText: String,
    var ceilingText: String,
    var statusLabel: String,
)

fun toScheduledTireScoreView(item: ScheduledTireScore): ScheduledTireScoreView {
    return ScheduledTireScoreView(
        codeText = item.code,
        title = item.title,
        dock = item.dock,
        graceDaysText = item.graceDays.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledTireScoreViews(items: MutableList<ScheduledTireScore>): MutableList<ScheduledTireScoreView> {
    val views = mutableListOf<ScheduledTireScoreView>()
    for (item in items) {
        views.add(toScheduledTireScoreView(item))
    }
    return views
}

fun sumScheduledTireScoreView(view: ScheduledTireScoreView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedGraceDays.toDouble() + parsedCeiling
}
