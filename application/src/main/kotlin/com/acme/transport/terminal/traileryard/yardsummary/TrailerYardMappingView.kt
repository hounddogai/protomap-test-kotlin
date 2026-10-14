package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard mapping. */
data class TrailerYardMappingView(
    var code: String,
    var name: String,
    var minimum: String,
    var ownerId: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toTrailerYardMappingView(item: TrailerYardMapping): TrailerYardMappingView {
    return TrailerYardMappingView(
        code = truncate(item.code, 120),
        name = item.name,
        minimum = "%.2f".format(item.minimum),
        ownerId = item.ownerId,
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardMappingViews(items: MutableList<TrailerYardMapping>): MutableList<TrailerYardMappingView> {
    val views = mutableListOf<TrailerYardMappingView>()
    for (item in items) {
        views.add(toTrailerYardMappingView(item))
    }
    return views
}

fun sumTrailerYardMappingView(view: TrailerYardMappingView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedMinimum + parsedEtaMinutes.toDouble()
}
