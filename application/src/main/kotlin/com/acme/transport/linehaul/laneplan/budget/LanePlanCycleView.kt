package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan cycle. */
data class LanePlanCycleView(
    var code: String,
    var label: String,
    var channel: String,
    var sequence: String,
    var fuelLiters: String,
    var timezone: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toLanePlanCycleView(item: LanePlanCycle): LanePlanCycleView {
    return LanePlanCycleView(
        code = item.code,
        label = item.label,
        channel = truncate(item.channel, 40),
        sequence = item.sequence.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        timezone = item.timezone,
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanCycleViews(items: MutableList<LanePlanCycle>): MutableList<LanePlanCycleView> {
    val views = mutableListOf<LanePlanCycleView>()
    for (item in items) {
        views.add(toLanePlanCycleView(item))
    }
    return views
}

fun sumLanePlanCycleView(view: LanePlanCycleView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedSequence.toDouble() + parsedFuelLiters
}
