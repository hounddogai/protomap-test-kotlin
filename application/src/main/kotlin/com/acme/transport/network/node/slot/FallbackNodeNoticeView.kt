package com.acme.transport.network.node.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback node notice. */
data class FallbackNodeNoticeView(
    var codeText: String,
    var nameText: String,
    var scoreText: String,
    var fuelLitersText: String,
    var notesText: String,
    var graceDays: String,
    var description: String,
    var retriesText: String,
    var durationMinutes: String,
    var version: String,
    var statusLabel: String,
)

fun toFallbackNodeNoticeView(item: FallbackNodeNotice): FallbackNodeNoticeView {
    return FallbackNodeNoticeView(
        codeText = item.code,
        nameText = truncate(item.name, 40),
        scoreText = "%.2f".format(item.score),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        notesText = truncate(item.notes, 64),
        graceDays = item.graceDays.toString(),
        description = item.description,
        retriesText = item.retries.toString(),
        durationMinutes = item.durationMinutes.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackNodeNoticeViews(items: MutableList<FallbackNodeNotice>): MutableList<FallbackNodeNoticeView> {
    val views = mutableListOf<FallbackNodeNoticeView>()
    for (item in items) {
        views.add(toFallbackNodeNoticeView(item))
    }
    return views
}

fun sumFallbackNodeNoticeView(view: FallbackNodeNoticeView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedScore + parsedFuelLiters + parsedGraceDays.toDouble()
}
