package com.acme.transport.crewing.roster.rostercycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback roster cycle. */
data class FallbackRosterCycleView(
    var codeText: String,
    var titleText: String,
    var etaMinutes: String,
    var carrier: String,
    var driverId: String,
    var pallets: String,
    var ratio: String,
    var endsAt: String,
    var minimum: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toFallbackRosterCycleView(item: FallbackRosterCycle): FallbackRosterCycleView {
    return FallbackRosterCycleView(
        codeText = item.code,
        titleText = item.title,
        etaMinutes = item.etaMinutes.toString(),
        carrier = truncate(item.carrier, 64),
        driverId = item.driverId,
        pallets = item.pallets.toString(),
        ratio = "%.2f".format(item.ratio),
        endsAt = item.endsAt.toString(),
        minimum = "%.2f".format(item.minimum),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRosterCycleViews(items: MutableList<FallbackRosterCycle>): MutableList<FallbackRosterCycleView> {
    val views = mutableListOf<FallbackRosterCycleView>()
    for (item in items) {
        views.add(toFallbackRosterCycleView(item))
    }
    return views
}

fun sumFallbackRosterCycleView(view: FallbackRosterCycleView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedEtaMinutes.toDouble() + parsedPallets.toDouble() + parsedRatio
}
