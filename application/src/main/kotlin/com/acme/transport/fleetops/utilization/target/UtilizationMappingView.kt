package com.acme.transport.fleetops.utilization.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization mapping. */
data class UtilizationMappingView(
    var code: String,
    var labelText: String,
    var shiftCodeText: String,
    var pinned: String,
    var pallets: String,
    var startsAt: String,
    var channel: String,
    var rate: String,
    var statusLabel: String,
)

fun toUtilizationMappingView(item: UtilizationMapping): UtilizationMappingView {
    return UtilizationMappingView(
        code = truncate(item.code, 80),
        labelText = item.label,
        shiftCodeText = item.shiftCode,
        pinned = "${item.pinned}",
        pallets = item.pallets.toString(),
        startsAt = item.startsAt.toString(),
        channel = truncate(item.channel, 120),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationMappingViews(items: MutableList<UtilizationMapping>): MutableList<UtilizationMappingView> {
    val views = mutableListOf<UtilizationMappingView>()
    for (item in items) {
        views.add(toUtilizationMappingView(item))
    }
    return views
}

fun sumUtilizationMappingView(view: UtilizationMappingView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedPallets.toDouble() + parsedStartsAt.toDouble() + parsedRate
}
