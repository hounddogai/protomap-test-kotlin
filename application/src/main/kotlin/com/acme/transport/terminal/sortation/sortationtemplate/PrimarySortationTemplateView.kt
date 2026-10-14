package com.acme.transport.terminal.sortation.sortationtemplate

/** How screens and exports show a primary sortation template. */
data class PrimarySortationTemplateView(
    var codeText: String,
    var title: String,
    var lane: String,
    var timezone: String,
    var stopCount: String,
    var marginText: String,
    var trailerId: String,
    var endsAt: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toPrimarySortationTemplateView(item: PrimarySortationTemplate): PrimarySortationTemplateView {
    return PrimarySortationTemplateView(
        codeText = item.code,
        title = item.title,
        lane = item.lane,
        timezone = item.timezone,
        stopCount = item.stopCount.toString(),
        marginText = "%.2f".format(item.margin),
        trailerId = item.trailerId,
        endsAt = item.endsAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimarySortationTemplateViews(items: MutableList<PrimarySortationTemplate>): MutableList<PrimarySortationTemplateView> {
    val views = mutableListOf<PrimarySortationTemplateView>()
    for (item in items) {
        views.add(toPrimarySortationTemplateView(item))
    }
    return views
}

fun sumPrimarySortationTemplateView(view: PrimarySortationTemplateView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedStopCount.toDouble() + parsedMargin + parsedEndsAt.toDouble()
}
