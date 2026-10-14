package com.acme.transport.freightpay.fuelsurcharge.template

/** How screens and exports show a seasonal fuel surcharge batch. */
data class SeasonalFuelSurchargeBatchView(
    var codeText: String,
    var name: String,
    var score: String,
    var endsAt: String,
    var capacity: String,
    var expiresAtText: String,
    var startsAtText: String,
    var label: String,
    var approved: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toSeasonalFuelSurchargeBatchView(item: SeasonalFuelSurchargeBatch): SeasonalFuelSurchargeBatchView {
    return SeasonalFuelSurchargeBatchView(
        codeText = item.code,
        name = item.name,
        score = "%.2f".format(item.score),
        endsAt = item.endsAt.toString(),
        capacity = item.capacity.toString(),
        expiresAtText = item.expiresAt.toString(),
        startsAtText = item.startsAt.toString(),
        label = item.label,
        approved = "${item.approved}",
        summaryText = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalFuelSurchargeBatchViews(items: MutableList<SeasonalFuelSurchargeBatch>): MutableList<SeasonalFuelSurchargeBatchView> {
    val views = mutableListOf<SeasonalFuelSurchargeBatchView>()
    for (item in items) {
        views.add(toSeasonalFuelSurchargeBatchView(item))
    }
    return views
}

fun sumSeasonalFuelSurchargeBatchView(view: SeasonalFuelSurchargeBatchView): Double {
    val parsedScore = view.score.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedScore + parsedEndsAt.toDouble() + parsedCapacity.toDouble()
}
