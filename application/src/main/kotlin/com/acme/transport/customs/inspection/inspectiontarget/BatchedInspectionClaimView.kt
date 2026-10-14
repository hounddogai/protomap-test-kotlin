package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched inspection claim. */
data class BatchedInspectionClaimView(
    var code: String,
    var name: String,
    var vehicleIdText: String,
    var distanceKmText: String,
    var threshold: String,
    var phone: String,
    var discount: String,
    var archived: String,
    var country: String,
    var rateText: String,
    var statusLabel: String,
)

fun toBatchedInspectionClaimView(item: BatchedInspectionClaim): BatchedInspectionClaimView {
    return BatchedInspectionClaimView(
        code = item.code,
        name = truncate(item.name, 40),
        vehicleIdText = item.vehicleId,
        distanceKmText = "%.2f".format(item.distanceKm),
        threshold = "%.2f".format(item.threshold),
        phone = truncate(item.phone, 40),
        discount = "%.2f".format(item.discount),
        archived = "${item.archived}",
        country = truncate(item.country, 80),
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedInspectionClaimViews(items: MutableList<BatchedInspectionClaim>): MutableList<BatchedInspectionClaimView> {
    val views = mutableListOf<BatchedInspectionClaimView>()
    for (item in items) {
        views.add(toBatchedInspectionClaimView(item))
    }
    return views
}

fun sumBatchedInspectionClaimView(view: BatchedInspectionClaimView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedDistanceKm + parsedThreshold + parsedDiscount
}
