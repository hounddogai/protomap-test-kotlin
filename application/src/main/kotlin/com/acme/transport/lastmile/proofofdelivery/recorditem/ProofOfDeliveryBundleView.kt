package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery bundle. */
data class ProofOfDeliveryBundleView(
    var code: String,
    var titleText: String,
    var depot: String,
    var sourceText: String,
    var distanceKm: String,
    var capacity: String,
    var ratio: String,
    var minimumText: String,
    var category: String,
    var statusLabel: String,
)

fun toProofOfDeliveryBundleView(item: ProofOfDeliveryBundle): ProofOfDeliveryBundleView {
    return ProofOfDeliveryBundleView(
        code = item.code,
        titleText = item.title,
        depot = item.depot,
        sourceText = truncate(item.source, 80),
        distanceKm = "%.2f".format(item.distanceKm),
        capacity = item.capacity.toString(),
        ratio = "%.2f".format(item.ratio),
        minimumText = "%.2f".format(item.minimum),
        category = truncate(item.category, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryBundleViews(items: MutableList<ProofOfDeliveryBundle>): MutableList<ProofOfDeliveryBundleView> {
    val views = mutableListOf<ProofOfDeliveryBundleView>()
    for (item in items) {
        views.add(toProofOfDeliveryBundleView(item))
    }
    return views
}

fun sumProofOfDeliveryBundleView(view: ProofOfDeliveryBundleView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedDistanceKm + parsedCapacity.toDouble() + parsedRatio
}
