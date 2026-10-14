package com.acme.transport.network.transittime.proposal

/** How screens and exports show a global transit time template. */
data class GlobalTransitTimeTemplateView(
    var codeText: String,
    var title: String,
    var visibleText: String,
    var startsAtText: String,
    var ownerId: String,
    var stopCountText: String,
    var durationMinutesText: String,
    var graceDays: String,
    var attempts: String,
    var statusLabel: String,
)

fun toGlobalTransitTimeTemplateView(item: GlobalTransitTimeTemplate): GlobalTransitTimeTemplateView {
    return GlobalTransitTimeTemplateView(
        codeText = item.code,
        title = item.title,
        visibleText = "${item.visible}",
        startsAtText = item.startsAt.toString(),
        ownerId = item.ownerId,
        stopCountText = item.stopCount.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        graceDays = item.graceDays.toString(),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTransitTimeTemplateViews(items: MutableList<GlobalTransitTimeTemplate>): MutableList<GlobalTransitTimeTemplateView> {
    val views = mutableListOf<GlobalTransitTimeTemplateView>()
    for (item in items) {
        views.add(toGlobalTransitTimeTemplateView(item))
    }
    return views
}

fun sumGlobalTransitTimeTemplateView(view: GlobalTransitTimeTemplateView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedStartsAt.toDouble() + parsedStopCount.toDouble() + parsedDurationMinutes.toDouble()
}
