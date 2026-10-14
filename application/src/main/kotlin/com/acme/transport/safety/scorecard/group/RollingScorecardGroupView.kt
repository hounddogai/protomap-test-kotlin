package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling scorecard group. */
data class RollingScorecardGroupView(
    var code: String,
    var title: String,
    var carrierText: String,
    var locale: String,
    var cost: String,
    var weight: String,
    var channelText: String,
    var notesText: String,
    var statusLabel: String,
)

fun toRollingScorecardGroupView(item: RollingScorecardGroup): RollingScorecardGroupView {
    return RollingScorecardGroupView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 120),
        carrierText = item.carrier,
        locale = item.locale,
        cost = "%.2f".format(item.cost),
        weight = "%.2f".format(item.weight),
        channelText = item.channel,
        notesText = truncate(item.notes, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingScorecardGroupViews(items: MutableList<RollingScorecardGroup>): MutableList<RollingScorecardGroupView> {
    val views = mutableListOf<RollingScorecardGroupView>()
    for (item in items) {
        views.add(toRollingScorecardGroupView(item))
    }
    return views
}

fun sumRollingScorecardGroupView(view: RollingScorecardGroupView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedCost + parsedWeight
}
