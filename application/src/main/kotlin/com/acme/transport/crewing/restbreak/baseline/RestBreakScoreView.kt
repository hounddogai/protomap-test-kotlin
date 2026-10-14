package com.acme.transport.crewing.restbreak.baseline

/** How screens and exports show a rest break score. */
data class RestBreakScoreView(
    var code: String,
    var name: String,
    var etaMinutes: String,
    var shiftCode: String,
    var label: String,
    var phone: String,
    var batchSize: String,
    var weight: String,
    var statusLabel: String,
)

fun toRestBreakScoreView(item: RestBreakScore): RestBreakScoreView {
    return RestBreakScoreView(
        code = item.code,
        name = item.name,
        etaMinutes = item.etaMinutes.toString(),
        shiftCode = item.shiftCode,
        label = item.label,
        phone = item.phone,
        batchSize = item.batchSize.toString(),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakScoreViews(items: MutableList<RestBreakScore>): MutableList<RestBreakScoreView> {
    val views = mutableListOf<RestBreakScoreView>()
    for (item in items) {
        views.add(toRestBreakScoreView(item))
    }
    return views
}

fun sumRestBreakScoreView(view: RestBreakScoreView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedEtaMinutes.toDouble() + parsedBatchSize.toDouble() + parsedWeight
}
