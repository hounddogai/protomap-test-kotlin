package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge mapping. */
data class EvChargeMappingView(
    var codeText: String,
    var name: String,
    var minimum: String,
    var revision: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toEvChargeMappingView(item: EvChargeMapping): EvChargeMappingView {
    return EvChargeMappingView(
        codeText = truncate(item.code, 64),
        name = item.name,
        minimum = "%.2f".format(item.minimum),
        revision = item.revision.toString(),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeMappingViews(items: MutableList<EvChargeMapping>): MutableList<EvChargeMappingView> {
    val views = mutableListOf<EvChargeMappingView>()
    for (item in items) {
        views.add(toEvChargeMappingView(item))
    }
    return views
}

fun sumEvChargeMappingView(view: EvChargeMappingView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedMinimum + parsedRevision.toDouble() + parsedStartsAt.toDouble()
}
