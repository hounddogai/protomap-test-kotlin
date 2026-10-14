package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional capacity profile. */
data class RegionalCapacityProfileView(
    var code: String,
    var name: String,
    var label: String,
    var contactEmail: String,
    var endsAtText: String,
    var archivedText: String,
    var vehicleIdText: String,
    var category: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toRegionalCapacityProfileView(item: RegionalCapacityProfile): RegionalCapacityProfileView {
    return RegionalCapacityProfileView(
        code = item.code,
        name = item.name,
        label = item.label,
        contactEmail = item.contactEmail,
        endsAtText = item.endsAt.toString(),
        archivedText = "${item.archived}",
        vehicleIdText = item.vehicleId,
        category = truncate(item.category, 40),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCapacityProfileViews(items: MutableList<RegionalCapacityProfile>): MutableList<RegionalCapacityProfileView> {
    val views = mutableListOf<RegionalCapacityProfileView>()
    for (item in items) {
        views.add(toRegionalCapacityProfileView(item))
    }
    return views
}

fun sumRegionalCapacityProfileView(view: RegionalCapacityProfileView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedEndsAt.toDouble() + parsedCeiling
}
