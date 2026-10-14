package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict backhaul target. */
data class StrictBackhaulTargetView(
    var code: String,
    var titleText: String,
    var archived: String,
    var capacityText: String,
    var sequenceText: String,
    var attempts: String,
    var endsAt: String,
    var weight: String,
    var statusLabel: String,
)

fun toStrictBackhaulTargetView(item: StrictBackhaulTarget): StrictBackhaulTargetView {
    return StrictBackhaulTargetView(
        code = truncate(item.code, 120),
        titleText = item.title,
        archived = "${item.archived}",
        capacityText = item.capacity.toString(),
        sequenceText = item.sequence.toString(),
        attempts = item.attempts.toString(),
        endsAt = item.endsAt.toString(),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictBackhaulTargetViews(items: MutableList<StrictBackhaulTarget>): MutableList<StrictBackhaulTargetView> {
    val views = mutableListOf<StrictBackhaulTargetView>()
    for (item in items) {
        views.add(toStrictBackhaulTargetView(item))
    }
    return views
}

fun sumStrictBackhaulTargetView(view: StrictBackhaulTargetView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedSequence = view.sequenceText.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedCapacity.toDouble() + parsedSequence.toDouble() + parsedAttempts.toDouble()
}
