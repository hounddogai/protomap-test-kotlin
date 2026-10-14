package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default parcel variance. */
data class DefaultParcelVarianceView(
    var code: String,
    var nameText: String,
    var score: String,
    var segment: String,
    var visible: String,
    var distanceKmText: String,
    var ceiling: String,
    var externalRef: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toDefaultParcelVarianceView(item: DefaultParcelVariance): DefaultParcelVarianceView {
    return DefaultParcelVarianceView(
        code = truncate(item.code, 64),
        nameText = truncate(item.name, 120),
        score = "%.2f".format(item.score),
        segment = item.segment,
        visible = "${item.visible}",
        distanceKmText = "%.2f".format(item.distanceKm),
        ceiling = "%.2f".format(item.ceiling),
        externalRef = item.externalRef,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultParcelVarianceViews(items: MutableList<DefaultParcelVariance>): MutableList<DefaultParcelVarianceView> {
    val views = mutableListOf<DefaultParcelVarianceView>()
    for (item in items) {
        views.add(toDefaultParcelVarianceView(item))
    }
    return views
}

fun sumDefaultParcelVarianceView(view: DefaultParcelVarianceView): Double {
    val parsedScore = view.score.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedScore + parsedDistanceKm + parsedCeiling
}
