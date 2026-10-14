package com.acme.transport.crewing.availability.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled availability cycle. */
data class ScheduledAvailabilityCycleView(
    var code: String,
    var name: String,
    var visibleText: String,
    var country: String,
    var pinned: String,
    var lane: String,
    var summary: String,
    var score: String,
    var fuelLitersText: String,
    var sequence: String,
    var statusLabel: String,
)

fun toScheduledAvailabilityCycleView(item: ScheduledAvailabilityCycle): ScheduledAvailabilityCycleView {
    return ScheduledAvailabilityCycleView(
        code = truncate(item.code, 120),
        name = item.name,
        visibleText = "${item.visible}",
        country = item.country,
        pinned = "${item.pinned}",
        lane = truncate(item.lane, 40),
        summary = item.summary,
        score = "%.2f".format(item.score),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledAvailabilityCycleViews(items: MutableList<ScheduledAvailabilityCycle>): MutableList<ScheduledAvailabilityCycleView> {
    val views = mutableListOf<ScheduledAvailabilityCycleView>()
    for (item in items) {
        views.add(toScheduledAvailabilityCycleView(item))
    }
    return views
}

fun sumScheduledAvailabilityCycleView(view: ScheduledAvailabilityCycleView): Double {
    val parsedScore = view.score.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedScore + parsedFuelLiters + parsedSequence.toDouble()
}
