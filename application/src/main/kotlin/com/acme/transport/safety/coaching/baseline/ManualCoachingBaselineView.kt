package com.acme.transport.safety.coaching.baseline

/** How screens and exports show a manual coaching baseline. */
data class ManualCoachingBaselineView(
    var codeText: String,
    var nameText: String,
    var quantityText: String,
    var weightText: String,
    var score: String,
    var expiresAt: String,
    var revision: String,
    var floorValue: String,
    var ceilingText: String,
    var source: String,
    var statusLabel: String,
)

fun toManualCoachingBaselineView(item: ManualCoachingBaseline): ManualCoachingBaselineView {
    return ManualCoachingBaselineView(
        codeText = item.code,
        nameText = item.name,
        quantityText = item.quantity.toString(),
        weightText = "%.2f".format(item.weight),
        score = "%.2f".format(item.score),
        expiresAt = item.expiresAt.toString(),
        revision = item.revision.toString(),
        floorValue = "%.2f".format(item.floorValue),
        ceilingText = "%.2f".format(item.ceiling),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCoachingBaselineViews(items: MutableList<ManualCoachingBaseline>): MutableList<ManualCoachingBaselineView> {
    val views = mutableListOf<ManualCoachingBaselineView>()
    for (item in items) {
        views.add(toManualCoachingBaselineView(item))
    }
    return views
}

fun sumManualCoachingBaselineView(view: ManualCoachingBaselineView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedWeight = view.weightText.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedQuantity.toDouble() + parsedWeight + parsedScore
}
