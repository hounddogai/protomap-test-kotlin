package com.acme.transport.terminal.crossdock.segment

/** How screens and exports show a cross dock segment. */
data class CrossDockSegmentView(
    var codeText: String,
    var label: String,
    var routeCode: String,
    var fuelLiters: String,
    var description: String,
    var statusLabel: String,
)

fun toCrossDockSegmentView(item: CrossDockSegment): CrossDockSegmentView {
    return CrossDockSegmentView(
        codeText = item.code,
        label = item.label,
        routeCode = item.routeCode,
        fuelLiters = "%.2f".format(item.fuelLiters),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockSegmentViews(items: MutableList<CrossDockSegment>): MutableList<CrossDockSegmentView> {
    val views = mutableListOf<CrossDockSegmentView>()
    for (item in items) {
        views.add(toCrossDockSegmentView(item))
    }
    return views
}

fun sumCrossDockSegmentView(view: CrossDockSegmentView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedFuelLiters
}
