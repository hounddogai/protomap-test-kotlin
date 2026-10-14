package com.acme.transport.crewing.qualification.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification override. */
data class QualificationOverrideView(
    var code: String,
    var titleText: String,
    var phoneText: String,
    var depotText: String,
    var cost: String,
    var floorValue: String,
    var active: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toQualificationOverrideView(item: QualificationOverride): QualificationOverrideView {
    return QualificationOverrideView(
        code = item.code,
        titleText = truncate(item.title, 120),
        phoneText = truncate(item.phone, 40),
        depotText = item.depot,
        cost = "%.2f".format(item.cost),
        floorValue = "%.2f".format(item.floorValue),
        active = "${item.active}",
        trailerIdText = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationOverrideViews(items: MutableList<QualificationOverride>): MutableList<QualificationOverrideView> {
    val views = mutableListOf<QualificationOverrideView>()
    for (item in items) {
        views.add(toQualificationOverrideView(item))
    }
    return views
}

fun sumQualificationOverrideView(view: QualificationOverrideView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedCost + parsedFloorValue
}
