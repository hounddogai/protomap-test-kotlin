package com.acme.transport.network.transittime.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global transit time cycle. */
data class GlobalTransitTimeCycleView(
    var codeText: String,
    var labelText: String,
    var contactEmail: String,
    var rate: String,
    var attemptsText: String,
    var weightText: String,
    var carrier: String,
    var sequence: String,
    var statusLabel: String,
)

fun toGlobalTransitTimeCycleView(item: GlobalTransitTimeCycle): GlobalTransitTimeCycleView {
    return GlobalTransitTimeCycleView(
        codeText = truncate(item.code, 80),
        labelText = item.label,
        contactEmail = item.contactEmail,
        rate = "%.2f".format(item.rate),
        attemptsText = item.attempts.toString(),
        weightText = "%.2f".format(item.weight),
        carrier = item.carrier,
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTransitTimeCycleViews(items: MutableList<GlobalTransitTimeCycle>): MutableList<GlobalTransitTimeCycleView> {
    val views = mutableListOf<GlobalTransitTimeCycleView>()
    for (item in items) {
        views.add(toGlobalTransitTimeCycleView(item))
    }
    return views
}

fun sumGlobalTransitTimeCycleView(view: GlobalTransitTimeCycleView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedRate + parsedAttempts.toDouble() + parsedWeight
}
