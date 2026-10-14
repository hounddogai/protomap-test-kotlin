package com.acme.transport.network.cutoff.segment

/** How screens and exports show a cutoff variance. */
data class CutoffVarianceView(
    var codeText: String,
    var name: String,
    var driverId: String,
    var graceDays: String,
    var source: String,
    var statusLabel: String,
)

fun toCutoffVarianceView(item: CutoffVariance): CutoffVarianceView {
    return CutoffVarianceView(
        codeText = item.code,
        name = item.name,
        driverId = item.driverId,
        graceDays = item.graceDays.toString(),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffVarianceViews(items: MutableList<CutoffVariance>): MutableList<CutoffVarianceView> {
    val views = mutableListOf<CutoffVarianceView>()
    for (item in items) {
        views.add(toCutoffVarianceView(item))
    }
    return views
}

fun sumCutoffVarianceView(view: CutoffVarianceView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    return parsedGraceDays.toDouble()
}
