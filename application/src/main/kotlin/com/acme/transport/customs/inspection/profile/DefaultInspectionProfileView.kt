package com.acme.transport.customs.inspection.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default inspection profile. */
data class DefaultInspectionProfileView(
    var code: String,
    var label: String,
    var revision: String,
    var ownerId: String,
    var capacityText: String,
    var rateText: String,
    var trailerId: String,
    var retries: String,
    var dock: String,
    var fuelLitersText: String,
    var source: String,
    var statusLabel: String,
)

fun toDefaultInspectionProfileView(item: DefaultInspectionProfile): DefaultInspectionProfileView {
    return DefaultInspectionProfileView(
        code = truncate(item.code, 64),
        label = item.label,
        revision = item.revision.toString(),
        ownerId = item.ownerId,
        capacityText = item.capacity.toString(),
        rateText = "%.2f".format(item.rate),
        trailerId = item.trailerId,
        retries = item.retries.toString(),
        dock = truncate(item.dock, 120),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        source = truncate(item.source, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultInspectionProfileViews(items: MutableList<DefaultInspectionProfile>): MutableList<DefaultInspectionProfileView> {
    val views = mutableListOf<DefaultInspectionProfileView>()
    for (item in items) {
        views.add(toDefaultInspectionProfileView(item))
    }
    return views
}

fun sumDefaultInspectionProfileView(view: DefaultInspectionProfileView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedCapacity = view.capacityText.toInt()
    val parsedRate = view.rateText.toDouble()
    return parsedRevision.toDouble() + parsedCapacity.toDouble() + parsedRate
}
