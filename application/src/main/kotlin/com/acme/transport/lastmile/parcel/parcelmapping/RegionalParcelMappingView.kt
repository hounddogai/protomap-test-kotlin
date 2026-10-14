package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional parcel mapping. */
data class RegionalParcelMappingView(
    var code: String,
    var name: String,
    var region: String,
    var notes: String,
    var startsAt: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toRegionalParcelMappingView(item: RegionalParcelMapping): RegionalParcelMappingView {
    return RegionalParcelMappingView(
        code = item.code,
        name = item.name,
        region = item.region,
        notes = truncate(item.notes, 64),
        startsAt = item.startsAt.toString(),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalParcelMappingViews(items: MutableList<RegionalParcelMapping>): MutableList<RegionalParcelMappingView> {
    val views = mutableListOf<RegionalParcelMappingView>()
    for (item in items) {
        views.add(toRegionalParcelMappingView(item))
    }
    return views
}

fun sumRegionalParcelMappingView(view: RegionalParcelMappingView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    return parsedStartsAt.toDouble()
}
