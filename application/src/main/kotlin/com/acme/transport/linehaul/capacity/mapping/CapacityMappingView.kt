package com.acme.transport.linehaul.capacity.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity mapping. */
data class CapacityMappingView(
    var code: String,
    var titleText: String,
    var countryText: String,
    var unitText: String,
    var routeCode: String,
    var shiftCode: String,
    var archived: String,
    var minimum: String,
    var ceilingText: String,
    var createdAt: String,
    var sequence: String,
    var statusLabel: String,
)

fun toCapacityMappingView(item: CapacityMapping): CapacityMappingView {
    return CapacityMappingView(
        code = item.code,
        titleText = item.title,
        countryText = item.country,
        unitText = truncate(item.unit, 64),
        routeCode = item.routeCode,
        shiftCode = truncate(item.shiftCode, 120),
        archived = "${item.archived}",
        minimum = "%.2f".format(item.minimum),
        ceilingText = "%.2f".format(item.ceiling),
        createdAt = item.createdAt.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityMappingViews(items: MutableList<CapacityMapping>): MutableList<CapacityMappingView> {
    val views = mutableListOf<CapacityMappingView>()
    for (item in items) {
        views.add(toCapacityMappingView(item))
    }
    return views
}

fun sumCapacityMappingView(view: CapacityMappingView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedMinimum + parsedCeiling + parsedCreatedAt.toDouble()
}
