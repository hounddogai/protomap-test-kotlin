package com.acme.transport.lastmile.parcel.review

/** How screens and exports show a weighted parcel threshold. */
data class WeightedParcelThresholdView(
    var code: String,
    var titleText: String,
    var revision: String,
    var rate: String,
    var durationMinutes: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toWeightedParcelThresholdView(item: WeightedParcelThreshold): WeightedParcelThresholdView {
    return WeightedParcelThresholdView(
        code = item.code,
        titleText = item.title,
        revision = item.revision.toString(),
        rate = "%.2f".format(item.rate),
        durationMinutes = item.durationMinutes.toString(),
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedParcelThresholdViews(items: MutableList<WeightedParcelThreshold>): MutableList<WeightedParcelThresholdView> {
    val views = mutableListOf<WeightedParcelThresholdView>()
    for (item in items) {
        views.add(toWeightedParcelThresholdView(item))
    }
    return views
}

fun sumWeightedParcelThresholdView(view: WeightedParcelThresholdView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedRevision.toDouble() + parsedRate + parsedDurationMinutes.toDouble()
}
