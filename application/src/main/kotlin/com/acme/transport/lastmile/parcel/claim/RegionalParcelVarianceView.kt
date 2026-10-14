package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional parcel variance. */
data class RegionalParcelVarianceView(
    var code: String,
    var name: String,
    var threshold: String,
    var minimumText: String,
    var timezone: String,
    var depot: String,
    var contactEmail: String,
    var capacityText: String,
    var statusLabel: String,
)

fun toRegionalParcelVarianceView(item: RegionalParcelVariance): RegionalParcelVarianceView {
    return RegionalParcelVarianceView(
        code = item.code,
        name = truncate(item.name, 80),
        threshold = "%.2f".format(item.threshold),
        minimumText = "%.2f".format(item.minimum),
        timezone = item.timezone,
        depot = truncate(item.depot, 64),
        contactEmail = item.contactEmail,
        capacityText = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalParcelVarianceViews(items: MutableList<RegionalParcelVariance>): MutableList<RegionalParcelVarianceView> {
    val views = mutableListOf<RegionalParcelVarianceView>()
    for (item in items) {
        views.add(toRegionalParcelVarianceView(item))
    }
    return views
}

fun sumRegionalParcelVarianceView(view: RegionalParcelVarianceView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    return parsedThreshold + parsedMinimum + parsedCapacity.toDouble()
}
