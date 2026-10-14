package com.acme.transport.linehaul.backhaul.template

/** How screens and exports show a fallback backhaul request. */
data class FallbackBackhaulRequestView(
    var code: String,
    var titleText: String,
    var channel: String,
    var score: String,
    var contactEmail: String,
    var windowDays: String,
    var visible: String,
    var updatedAtText: String,
    var margin: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toFallbackBackhaulRequestView(item: FallbackBackhaulRequest): FallbackBackhaulRequestView {
    return FallbackBackhaulRequestView(
        code = item.code,
        titleText = item.title,
        channel = item.channel,
        score = "%.2f".format(item.score),
        contactEmail = item.contactEmail,
        windowDays = item.windowDays.toString(),
        visible = "${item.visible}",
        updatedAtText = item.updatedAt.toString(),
        margin = "%.2f".format(item.margin),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackBackhaulRequestViews(items: MutableList<FallbackBackhaulRequest>): MutableList<FallbackBackhaulRequestView> {
    val views = mutableListOf<FallbackBackhaulRequestView>()
    for (item in items) {
        views.add(toFallbackBackhaulRequestView(item))
    }
    return views
}

fun sumFallbackBackhaulRequestView(view: FallbackBackhaulRequestView): Double {
    val parsedScore = view.score.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedScore + parsedWindowDays.toDouble() + parsedUpdatedAt.toDouble()
}
