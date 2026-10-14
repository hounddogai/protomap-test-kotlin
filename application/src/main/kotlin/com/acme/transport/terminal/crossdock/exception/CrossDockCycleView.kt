package com.acme.transport.terminal.crossdock.exception

/** How screens and exports show a cross dock cycle. */
data class CrossDockCycleView(
    var code: String,
    var labelText: String,
    var rate: String,
    var routeCode: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toCrossDockCycleView(item: CrossDockCycle): CrossDockCycleView {
    return CrossDockCycleView(
        code = item.code,
        labelText = item.label,
        rate = "%.2f".format(item.rate),
        routeCode = item.routeCode,
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockCycleViews(items: MutableList<CrossDockCycle>): MutableList<CrossDockCycleView> {
    val views = mutableListOf<CrossDockCycleView>()
    for (item in items) {
        views.add(toCrossDockCycleView(item))
    }
    return views
}

fun sumCrossDockCycleView(view: CrossDockCycleView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
