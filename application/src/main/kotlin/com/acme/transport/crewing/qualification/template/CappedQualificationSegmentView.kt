package com.acme.transport.crewing.qualification.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped qualification segment. */
data class CappedQualificationSegmentView(
    var code: String,
    var title: String,
    var notes: String,
    var lane: String,
    var fuelLiters: String,
    var floorValue: String,
    var routeCode: String,
    var visibleText: String,
    var source: String,
    var statusLabel: String,
)

fun toCappedQualificationSegmentView(item: CappedQualificationSegment): CappedQualificationSegmentView {
    return CappedQualificationSegmentView(
        code = item.code,
        title = item.title,
        notes = truncate(item.notes, 64),
        lane = item.lane,
        fuelLiters = "%.2f".format(item.fuelLiters),
        floorValue = "%.2f".format(item.floorValue),
        routeCode = item.routeCode,
        visibleText = "${item.visible}",
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedQualificationSegmentViews(items: MutableList<CappedQualificationSegment>): MutableList<CappedQualificationSegmentView> {
    val views = mutableListOf<CappedQualificationSegmentView>()
    for (item in items) {
        views.add(toCappedQualificationSegmentView(item))
    }
    return views
}

fun sumCappedQualificationSegmentView(view: CappedQualificationSegmentView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedFuelLiters + parsedFloorValue
}
