package com.acme.transport.terminal.crossdock.segment

/** How screens and exports show a cross dock rule. */
data class CrossDockRuleView(
    var codeText: String,
    var name: String,
    var routeCode: String,
    var weight: String,
    var revisionText: String,
    var batchSize: String,
    var score: String,
    var shiftCode: String,
    var loadKg: String,
    var reviewer: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toCrossDockRuleView(item: CrossDockRule): CrossDockRuleView {
    return CrossDockRuleView(
        codeText = item.code,
        name = item.name,
        routeCode = item.routeCode,
        weight = "%.2f".format(item.weight),
        revisionText = item.revision.toString(),
        batchSize = item.batchSize.toString(),
        score = "%.2f".format(item.score),
        shiftCode = item.shiftCode,
        loadKg = "%.2f".format(item.loadKg),
        reviewer = item.reviewer,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockRuleViews(items: MutableList<CrossDockRule>): MutableList<CrossDockRuleView> {
    val views = mutableListOf<CrossDockRuleView>()
    for (item in items) {
        views.add(toCrossDockRuleView(item))
    }
    return views
}

fun sumCrossDockRuleView(view: CrossDockRuleView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedRevision = view.revisionText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedWeight + parsedRevision.toDouble() + parsedBatchSize.toDouble()
}
